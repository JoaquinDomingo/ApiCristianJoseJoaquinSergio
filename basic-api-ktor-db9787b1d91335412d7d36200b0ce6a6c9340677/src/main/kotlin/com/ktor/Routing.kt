package com.ktor

import com.domain.models.Employee
import com.domain.models.Salary
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        /*
        ktor evalua los endpoint por orden.
         */

        get("/") {
            call.respondText("Hello World!")
        }


        /*
        En esta ruta, comprobamos diferentes parámetros:
        1.- Que no tenga ningún parámetro. Devuelve todos los empleados sin filtro.
        2.- Que le pasemos el dni por query. Devuelve ese empleado . Lo tengo de ejemplo, ya que no debería utilizar una query para un recurso específico.
        3.- Que le pasemos el salario por query. Devuelve todos los empleados que tienen dicho salario.
         */
        get("/employee"){

            val employeeDni = call.request.queryParameters["dni"]
            logger.warn("El Dni tiene de valor $employeeDni")
            if (employeeDni != null) {
                val employee = ProviderUseCase.getEmployeeByDni(employeeDni)
                if (employee == null) {
                    call.respond(HttpStatusCode.NotFound, "Empleado no encontrado")
                } else {
                    call.respond(employee)
                }
                return@get
            }

            //comprobamos si hemos pasado el parámetro salary
            val salary = call.request.queryParameters["salary"]
            logger.warn("El salario pasado es $salary")
            if (salary != null) {
                try{
                    val s = salary.uppercase()
                    logger.warn("salario seleccionado: ${Salary.valueOf(s)}")
                    val employees = ProviderUseCase.getEmployeeBySalary(Salary.valueOf(salary.uppercase()))  //Salary.valueOf(String.upperecase()) convierte un string a Enum
                    call.respond(employees)
                }catch (e: IllegalArgumentException){
                    call.respond(HttpStatusCode.BadRequest, "El salario no corresponde con los tipos utilizados")
                }

            }else{ //No hemos pasado ninguna query
                val employees = ProviderUseCase.getAllEmployees()  //Ya tengo todos los empleados.
                call.respond(employees)
            }
        }

        /*
        Endpoint que no es recomendable, porque no se debe utilizar parámetros de url para filtrar. Para eso están los de consulta.
         */
        get("/employee/{employeeDni}") {

            //Comprobamos si se ha pasado el dni por parámetro
            val employeeDni = call.parameters["employeeDni"]
            if (employeeDni == null){
                call.respond(HttpStatusCode.BadRequest, "Debes pasar el dni a buscar") //Montamos una respuesta con código 400.
                return@get  //finalizamos en endpoint y mandamos inmediantamente la respuesta.
            }

            val employee = ProviderUseCase.getEmployeeByDni(employeeDni)
            if (employee ==null){
                call.respond(HttpStatusCode.NotFound,"Empleado no encontrado")  //Montamos un 404 de no encontrado.
                return@get //finalizamos en endpoint y mandamos inmediantamente la respuesta.
            }
            call.respond(employee)  //mandamos el empleado como respuesta al cliente.
        }




        post("/employee"){
            try{
                val emp = call.receive<Employee>()  //Leemos el cuerpo de la solicitud como un objeto Employee
                val res = ProviderUseCase.insertEmployee(emp)
                if (! res){
                    call.respond(HttpStatusCode.Conflict, "El empleado no pudo insertarse. Puede que ya exista")
                    return@post //aunque no es necesario, es buena práctica ponerlo para no olvidarlo, pero no hay más lógica.
                }
                call.respond(HttpStatusCode.Created, "Se ha insertado correctamente con dni =  ${emp.dni}")
            } catch (e : IllegalStateException){
                call.respond(HttpStatusCode.BadRequest, "Error en el formato de envío de datos o lectura del cuerpo.")
            } catch (e:JsonConvertException){
                call.respond(HttpStatusCode.BadRequest," Problemas en la conversión json")
            }

        }

        patch("/employee/{employeeDni}") {
            try{
                val dni = call.parameters["employeeDni"]
                dni?.let{
                    val updateEmployee = call.receive<UpdateEmployee>()
                    val res = ProviderUseCase.updateEmployee(updateEmployee, dni)
                    if (! res){
                        call.respond(HttpStatusCode.Conflict, "El empleado no pudo modificarse. Puede que no exista")
                        return@patch //aunque no es necesario, es buena práctica ponerlo para no olvidarlo, pero no hay más lógica.
                    }
                    call.respond(HttpStatusCode.Created, "Se ha actualizado correctamente con dni =  ${dni}")
                }?: run{
                    call.respond(HttpStatusCode.BadRequest,"Debes identificar el empleado")
                    return@patch //aunque no es necesario, es buena práctica ponerlo para no olvidarlo, pero no hay más lógica.
                }
            } catch (e: IllegalStateException){
                call.respond(HttpStatusCode.BadRequest,"Error en el formado de envío de los datos o lectura del cuerpo.")
            } catch (e: JsonConvertException){
                call.respond(HttpStatusCode.BadRequest,"Error en el formado de json")
            }
        }


        delete("/employee/{employeeDni}") {
            val dni = call.parameters["employeeDni"]
            logger.warn("Queremos borrar el empleado con dni $dni")
            dni?.let{
                val res = ProviderUseCase.deleteEmployee(dni)
                if (! res){
                    call.respond(HttpStatusCode.NotFound,"Empleado no encontrado para borrar")  //Montamos un 404 de no encontrado.
                }else{
                    call.respond(HttpStatusCode.NoContent,)
                }
            }?:run{
                call.respond(HttpStatusCode.NoContent,"Debes identintificar el empleado")
            }
            return@delete
        }





        // Static plugin. Try to access `/static/index.html`
        staticResources("/static", "static")
    }
}



/*
1.- Utilizaremos un directorio para servir recursos estáticos, como imágenes, html, css, javaScript
    - Primer parámetro static, como prefijo de la url. http://localhost/static
    - Segundo parámetro static, como directorio dentro del proyecto donde buscará el recurso. Lo llamamos static.
    - Tenemos un fichero index.html dentro de static. Lo utilizaremos más adelante.

2.- Añadimos una ruta con employee. Ejemplo http://localhost/employee
   - Necesitamos mandar una solicitud HTTP que el servidor deberá manejar. La solicitud es de tipo respond. Significa
   que el servidor mandará una respuesta al cliente. En dicha respuesta, tenemos una lista de objetos Employee
   que al tener la serialización, la mandará en formato json. Para ello, utiliza el pluggin de serialización (ContentNegotiation)
   configurada previamente para convertir automáticamente los objetos kotlin en json.

   - El flujo es:
        1.- El cliente hace una solicitud GET a employee
        2.- ktor encuentra la routa get("/employee")
        3.- El servidor crea una lista de objetos.
        4.- La lista se convierte automaticamente en Json gracias a CpontentNegotiation.
        5.- El servidor manda una respuesta al cliente.

        El pluggin Serialization, define como convertir los objetos kotlin a json y viceversa.
        El pluggin ContentNegotiation, gestiona el formato de respuesta o solicitudes dependiendo de las cabeceras
            enviadas por el cliente. HTTP Acept. Si el cliente envía Accpt:application/json, éste responderá automáticamente
            en json. Necesitaríamos otro Serializacón para xml y por tanto añadir soporte XML en el plugin ContentNegotiation.

        Por defecto, el servidor enviará la respuesta en JSON si el cliente en su solicitud no incluye una cabecera
        Accept con application/json
 */