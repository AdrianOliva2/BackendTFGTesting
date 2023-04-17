package dam.adrianoliva.data.employee

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Employee(
    @BsonId val id: ObjectId = ObjectId(),
    val userName: String,
    val email: String,
    val password: String,
    val phone: String,
    val department: String,
    val salt: String
)
