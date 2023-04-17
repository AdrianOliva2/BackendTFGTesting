package dam.adrianoliva.data.employee

import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class EmployeeMongoDTS(
    db: CoroutineDatabase
): EmployeeDTS {

    private val employees = db.getCollection<Employee>()

    override suspend fun getEmployeeByEmail(email: String): Employee? {
        return employees.findOne(Employee::email eq email)
    }

    override suspend fun getEmployeeByUserName(userName: String): Employee? {
        return employees.findOne(Employee::userName eq userName)
    }

    override suspend fun getEmployeeById(id: String): Employee? {
        return employees.findOne(Employee::id eq ObjectId(id))
    }

    override suspend fun getEmployeeByPhone(phone: String): Employee? {
        return employees.findOne(Employee::phone eq phone)
    }

    override suspend fun createEmployee(employee: Employee): Boolean {
        if (getEmployeeByEmail(employee.email) != null) return false
        if (getEmployeeByUserName(employee.userName) != null) return false
        if (getEmployeeByPhone(employee.phone) != null) return false
        return employees.insertOne(employee).wasAcknowledged()
    }

    override suspend fun updateEmployee(employee: Employee): Boolean {
        return employees.updateOne(Employee::id eq employee.id, employee).wasAcknowledged()
    }

    override suspend fun deleteEmployee(id: String): Boolean {
        return employees.deleteOne(Employee::id eq ObjectId(id)).wasAcknowledged()
    }

    override suspend fun getAllEmployees(): List<Employee> {
        return employees.find().toList()
    }
}