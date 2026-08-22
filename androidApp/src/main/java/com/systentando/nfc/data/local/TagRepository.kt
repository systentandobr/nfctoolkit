package com.systentando.nfc.data.local

import com.systentando.nfc.domain.model.NDEFRecord
import com.systentando.nfc.domain.model.NFCTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class TagRepository @Inject constructor(
    private val tagDao: TagDao
) {
    fun getAllTags(): Flow<List<NFCTag>> = tagDao.getAll().map { entities ->
        entities.map { it.toModel() }
    }

    suspend fun saveTag(tag: NFCTag) {
        tagDao.insert(tag.toEntity())
    }

    suspend fun deleteTag(id: String) {
        tagDao.getById(id)?.let { tagDao.delete(it) }
    }

    suspend fun deleteAll() = tagDao.deleteAll()

    private fun NFCTag.toEntity(): TagEntity {
        return TagEntity(
            id = "${uid}_${readAt}",
            uid = uid,
            chipType = chipType,
            technology = technology,
            sizeBytes = sizeBytes,
            usedBytes = usedBytes,
            isNDEF = isNDEF,
            recordsJson = records.toJson(),
            rawHex = rawHex,
            readAt = readAt,
            nickname = nickname
        )
    }

    private fun TagEntity.toModel(): NFCTag {
        return NFCTag(
            uid = uid,
            chipType = chipType,
            technology = technology,
            sizeBytes = sizeBytes,
            usedBytes = usedBytes,
            isNDEF = isNDEF,
            records = recordsJson.fromJson(),
            rawHex = rawHex,
            readAt = readAt,
            nickname = nickname
        )
    }

    private fun List<NDEFRecord>.toJson(): String {
        val array = JSONArray()
        forEach { record ->
            val obj = JSONObject().apply {
                put("type", record::class.java.simpleName)
                put("id", record.id)
                put("tnf", record.tnf.toInt())
                put("recordType", record.type)
                when (record) {
                    is NDEFRecord.Text -> {
                        put("text", record.text)
                        put("language", record.language)
                    }
                    is NDEFRecord.Uri -> {
                        put("uri", record.uri)
                    }
                    is NDEFRecord.Mime -> {
                        put("mimeType", record.type)
                        put("payload", record.payload)
                    }
                    is NDEFRecord.External -> {
                        put("payload", record.payload.toList())
                    }
                    is NDEFRecord.Unknown -> {
                        put("payload", record.payload.toList())
                    }
                }
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun String.fromJson(): List<NDEFRecord> {
        return try {
            val array = JSONArray(this)
            val result = mutableListOf<NDEFRecord>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val type = obj.getString("type")
                val id = obj.optString("id", "")
                val tnf = obj.optInt("tnf", 0).toShort()
                val recordType = obj.optString("recordType", "")
                val record = when (type) {
                    "Text" -> NDEFRecord.Text(
                        id = id,
                        text = obj.optString("text", ""),
                        language = obj.optString("language", "en")
                    )
                    "Uri" -> NDEFRecord.Uri(
                        id = id,
                        uri = obj.optString("uri", "")
                    )
                    "Mime" -> NDEFRecord.Mime(
                        id = id,
                        type = obj.optString("mimeType", recordType),
                        payload = obj.optString("payload", "")
                    )
                    "External" -> NDEFRecord.External(
                        id = id,
                        type = recordType,
                        payload = obj.getJSONArray("payload").let { arr ->
                            ByteArray(arr.length()) { arr.getInt(it).toByte() }
                        }
                    )
                    "Unknown" -> NDEFRecord.Unknown(
                        id = id,
                        tnf = tnf,
                        type = recordType,
                        payload = obj.getJSONArray("payload").let { arr ->
                            ByteArray(arr.length()) { arr.getInt(it).toByte() }
                        }
                    )
                    else -> NDEFRecord.Unknown(
                        id = id,
                        tnf = tnf,
                        type = recordType,
                        payload = byteArrayOf()
                    )
                }
                result.add(record)
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }
}
