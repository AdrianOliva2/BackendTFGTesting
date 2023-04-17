package dam.adrianoliva.data.employee

interface EmployeeDTS {
    suspend fun getEmployeeByEmail(email: String): Employee?
    suspend fun getEmployeeByUserName(userName: String): Employee?
    suspend fun getEmployeeById(id: String): Employee?
    suspend fun getEmployeeByPhone(phone: String): Employee?
    suspend fun createEmployee(employee: Employee): Boolean
    suspend fun updateEmployee(employee: Employee): Boolean
    suspend fun deleteEmployee(id: String): Boolean
    suspend fun getAllEmployees(): List<Employee>
}