package dev.vskelk.cdf.core.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.vskelk.cdf.core.database.dao.*
import dev.vskelk.cdf.core.database.entity.*
import dev.vskelk.cdf.core.domain.model.*
import dev.vskelk.cdf.core.domain.repository.*
import dev.vskelk.cdf.core.datastore.PreferencesDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BootstrapRepositoryImpl - Implementación del repositorio de bootstrap
 *
 * Carga los datos seed desde archivos JSON en assets/seed/ hacia la base de datos Room.
 * Per spec: "La IA nunca escribe conocimiento canónico directamente."
 * El seed es conocimiento canónico validado provisto por el usuario.
 */
@Singleton
class BootstrapRepositoryImpl @Inject constructor(
    private val context: Context,
    private val ontologyDao: OntologyDao,
    private val normativeDao: NormativeDao,
    private val reactivoDao: ReactivoDao,
    private val preferencesDataSource: PreferencesDataSource
) : BootstrapRepository {

    private val gson = Gson()

    override val bootstrapState: Flow<BootstrapState> = flow {
        emit(BootstrapState.Checking)

        try {
            val manifest = getManifestJson()
            val currentVersion = preferencesDataSource.seedVersionApplied.first()
            val needsSeeding = currentVersion != manifest.version || !meetsMinimumCounts(manifest)

            if (needsSeeding) {
                // Ejecutar seeding real
                emit(BootstrapState.Seeding("Iniciando carga de datos...", 0f))

                loadSeedData { message, progress ->
                    emit(BootstrapState.Seeding(message, progress))
                }

                // Guardar versión aplicada
                preferencesDataSource.setSeedVersionApplied(manifest.version)

                emit(BootstrapState.Ready)
            } else {
                emit(BootstrapState.Ready)
            }
        } catch (e: Exception) {
            emit(BootstrapState.Error(e.message ?: "Error desconocido", canRetry = true))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Carga los datos de seed en la base de datos
     */
    private suspend fun loadSeedData(onProgress: (String, Float) -> Unit) = withContext(Dispatchers.IO) {
        // 1. Cargar fuentes normativas
        onProgress("Cargando fuentes normativas...", 0.05f)
        val sources = loadNormativaSources()
        sources.forEach { source ->
            normativeDao.insertSource(
                DocumentSourceEntity(
                    id = source.id,
                    nombreCompleto = source.name,
                    abreviatura = source.code,
                    tipo = source.type,
                    isOficial = true,
                    createdAt = source.active_since
                )
            )
        }

        // 2. Cargar fragmentos normativos
        onProgress("Cargando fragmentos normativos...", 0.15f)
        val fragments = loadNormativaFragments()
        fragments.forEachIndexed { index, fragment ->
            val source = sources.find { it.id == fragment.source_id }
            normativeDao.insertFragment(
                NormativeFragmentEntity(
                    id = fragment.id,
                    content = fragment.content,
                    source = source?.code ?: "UNKNOWN",
                    articleRef = fragment.article_ref,
                    sourceType = source?.type ?: "LEY",
                    certainty = ExtractionCertainty.ALTA,
                    areaExamen = "TECNICO",
                    relatedNodesJson = null,
                    versionId = fragment.version_id,
                    vigenciaDesde = fragment.vigencia_desde,
                    vigenciaHasta = fragment.vigencia_hasta,
                    status = fragment.status,
                    reemplazadoPorId = fragment.replaced_by_id,
                    confidenceCount = 1,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
            onProgress("Cargando fragmentos normativos... ${index + 1}/${fragments.size}", 0.15f + 0.2f * (index + 1) / fragments.size)
        }

        // 3. Cargar nodos ontológicos
        onProgress("Cargando ontología...", 0.4f)
        val nodes = loadOntologiaNodes()

        // Primero, crear el cargo VOE por defecto
        ontologyDao.insertCargo(
            CargoEntity(
                id = 1,
                nombre = "Vocal de Organización Electoral",
                descripcion = "Cargo de Técnico de Organización Electoral, Distrito 06, Chihuahua",
                areaExamen = "TECNICO",
                nivel = "Vocalía",
                isDefault = true
            )
        )

        // Insertar órganos básicos
        ontologyDao.insertOrgano(
            OrganoEntity(
                id = 1,
                nombre = "Junta Distrital Ejecutiva",
                tipo = "JUNTA",
                nivel = "DISTRITAL",
                ambito = "INE"
            )
        )
        ontologyDao.insertOrgano(
            OrganoEntity(
                id = 2,
                nombre = "Consejo Distrital",
                tipo = "CONSEJO",
                nivel = "DISTRITAL",
                ambito = "INE"
            )
        )

        // Insertar nodos ontológicos
        nodes.forEachIndexed { index, node ->
            val nodeType = when (node.node_type.uppercase()) {
                "CARGO" -> OntologyNodeTypes.CARGO
                "ORGANO" -> OntologyNodeTypes.ORGANO
                "TEMA" -> OntologyNodeTypes.TEMA
                "SUBTEMA" -> OntologyNodeTypes.SUBTEMA
                "COMPETENCIA" -> OntologyNodeTypes.COMPETENCIA
                "PATRON_ERROR" -> OntologyNodeTypes.PATRON_ERROR
                else -> node.node_type.uppercase()
            }

            ontologyDao.insertNode(
                OntologyNodeEntity(
                    id = node.id,
                    nodeType = nodeType,
                    name = node.label,
                    description = node.description,
                    parentId = node.parent_id,
                    cargoId = if (nodeType == OntologyNodeTypes.CARGO) 1L else null,
                    primaryFundamentoRef = null,
                    weight = node.confidence.toFloat(),
                    displayOrder = index,
                    isActive = node.is_active,
                    updatedAt = System.currentTimeMillis()
                )
            )
            onProgress("Cargando ontología... ${index + 1}/${nodes.size}", 0.4f + 0.2f * (index + 1) / nodes.size)
        }

        // 4. Cargar relaciones ontológicas
        onProgress("Cargando relaciones ontológicas...", 0.65f)
        val edges = loadOntologiaEdges()
        edges.forEachIndexed { index, edge ->
            ontologyDao.insertRelation(
                OntologyRelationEntity(
                    sourceNodeId = edge.source_id,
                    targetNodeId = edge.target_id,
                    relationType = edge.relation_type,
                    description = null
                )
            )
            onProgress("Cargando relaciones... ${index + 1}/${edges.size}", 0.65f + 0.1f * (index + 1) / edges.size)
        }

        // 5. Cargar reactivos con opciones
        onProgress("Cargando reactivos...", 0.8f)
        val reactivos = loadReactivos()
        reactivos.forEachIndexed { index, reactivo ->
            val reactivoEntity = ReactivoEntity(
                id = reactivo.id,
                enunciado = reactivo.enunciado,
                modulo = reactivo.modulo,
                examArea = reactivo.exam_area,
                temaId = reactivo.tema_id,
                subtemaId = reactivo.subtema_id ?: reactivo.tema_id,
                ontologyNodeId = reactivo.subtema_id,
                tipoReactivo = reactivo.tipo_reactivo,
                nivelCognitivo = reactivo.nivel_cognitivo,
                dificultad = reactivo.dificultad.toFloat(),
                patronErrorId = reactivo.patron_error_id,
                citaTextual = reactivo.cita_textual,
                vigenciaDesde = reactivo.vigencia_desde,
                vigenciaHasta = reactivo.vigencia_hasta,
                origen = reactivo.origen,
                status = reactivo.status,
                invalidationReason = reactivo.invalidation_reason,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            reactivoDao.insertReactivo(reactivoEntity)

            // Insertar opciones del reactivo
            val opciones = reactivo.options.mapIndexed { optIndex, option ->
                ReactivoOptionEntity(
                    id = option.id,
                    reactivoId = reactivo.id,
                    texto = option.texto,
                    isCorrect = option.is_correct,
                    explicacion = option.explicacion,
                    distractorTipo = option.distractor_tipo,
                    displayOrder = optIndex
                )
            }
            reactivoDao.insertOptions(opciones)

            // Crear cross-reference con fragmentos normativos
            reactivo.fundamento_id?.let { fragId ->
                normativeDao.insertReactivoFragmentCrossRef(
                    ReactivoFragmentCrossRef(
                        reactivoId = reactivo.id,
                        fragmentId = fragId,
                        isPrimary = true
                    )
                )
            }

            onProgress("Cargando reactivos... ${index + 1}/${reactivos.size}", 0.8f + 0.2f * (index + 1) / reactivos.size)
        }

        onProgress("Carga completada", 1.0f)
    }

    // ===== MÉTODOS DE CARGA DE JSON =====

    private inline fun <reified T> parseJsonFile(filename: String): T {
        val json = context.assets.open("seed/$filename")
            .bufferedReader()
            .use { it.readText() }
        return gson.fromJson(json, object : TypeToken<T>() {}.type)
    }

    private fun getManifestJson(): SeedManifest {
        val json = context.assets.open("seed/seed_manifest.json")
            .bufferedReader()
            .use { it.readText() }
        return gson.fromJson(json, SeedManifest::class.java)
    }

    private fun loadNormativaSources(): List<SeedNormativaSource> {
        return try {
            parseJsonFile("normativa_sources.json")
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun loadNormativaFragments(): List<SeedNormativaFragment> {
        return try {
            parseJsonFile("normativa_fragments.json")
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun loadOntologiaNodes(): List<SeedOntologiaNode> {
        return try {
            parseJsonFile("ontologia.json")
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun loadOntologiaEdges(): List<SeedOntologiaEdge> {
        return try {
            parseJsonFile("ontologia_edges.json")
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun loadReactivos(): List<SeedReactivo> {
        return try {
            parseJsonFile("reactivos.json")
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ===== IMPLEMENTACIÓN DE INTERFAZ =====

    override suspend fun needsSeeding(): Boolean {
        val manifest = getManifestJson()
        return preferencesDataSource.seedVersionApplied.first() != manifest.version ||
                !meetsMinimumCounts(manifest)
    }

    override suspend fun getSeedVersion(): String? {
        return preferencesDataSource.seedVersionApplied.first().takeIf { it.isNotEmpty() }
    }

    override suspend fun getManifest(): String {
        return context.assets.open("seed/seed_manifest.json")
            .bufferedReader()
            .use { it.readText() }
    }

    private suspend fun meetsMinimumCounts(manifest: SeedManifest): Boolean {
        val reactivos = reactivoDao.getActiveReactivoCount()
        val normativa = normativeDao.getVigenteFragmentCount()
        val ontologia = ontologyDao.getActiveNodeCount()

        return reactivos >= manifest.minReactivos &&
                normativa >= manifest.minNormativa &&
                ontologia >= manifest.minOntologia
    }
}
