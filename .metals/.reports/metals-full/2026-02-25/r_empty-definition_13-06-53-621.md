error id: file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/security/jwt/AuthResponse.java:
file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/security/jwt/AuthResponse.java
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 410
uri: file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/security/jwt/AuthResponse.java
text:
```scala
package es.marta.tfg.carescan.security.jwt;

public class AuthResponse {

	private Status status;
	private String message;
	private String error;

	public enum Status {
		SUCCESS, FAILURE
	}

	public AuthResponse() {
	}

	public AuthResponse(Status status, String message) {
		this.status = status;
		this.message = message;
	}

	public AuthResponse(Status status, String message, String error) {
		this.status@@ = status;
		this.message = message;
		this.error = error;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	@Override
	public String toString() {
		return "LoginResponse [status=" + status + ", message=" + message + ", error=" + error + "]";
	}

}

```


#### Short summary: 

empty definition using pc, found symbol in pc: 