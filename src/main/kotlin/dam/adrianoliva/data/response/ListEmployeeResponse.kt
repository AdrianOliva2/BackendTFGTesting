package dam.adrianoliva.data.response

import kotlinx.serialization.Serializable

@Serializable
data class ListEmployeeResponse(
    val employees: List<EmployeeResponse>
)
