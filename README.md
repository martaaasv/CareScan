# CareScan - Instrucciones de ejecucion para evaluacion

Este proyecto tiene dos partes que deben estar arrancadas a la vez:

- Aplicacion web Java Spring Boot: interfaz, usuarios, consultas, resultados y base de datos.
- API Python de inferencia: carga el modelo de red neuronal y devuelve la clasificacion y el mapa Grad-CAM.

La aplicacion Java llama a la API Python en `http://127.0.0.1:8000/predict`, por lo que primero debe arrancarse la API Python y despues la aplicacion Java.

## 1. Requisitos

Instalar en el equipo:

- Java JDK 21.
- Maven 3.9 o superior.
- MySQL Server 8.x.
- Python oficial de Windows 3.13, o una version compatible con TensorFlow 2.21.
- Navegador web.

En Windows, se recomienda usar PowerShell.

## 2. Estructura esperada

Tras descomprimir el ZIP, la carpeta debe contener, entre otros:

```text
TFG/
  pom.xml
  src/
  python-model-api/
    app.py
    requirements.txt
    models/
      model-32-0.99-0.04.h5
```

El fichero del modelo es obligatorio:

```text
python-model-api/models/model-32-0.99-0.04.h5
```

Si el ZIP entregado no incluye el modelo por limite de tamano, copiar manualmente el fichero `.h5` en esa ruta antes de arrancar la API Python.

## 3. Preparar MySQL

Crear la base de datos vacia:

```sql
CREATE DATABASE carescan_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

La configuracion por defecto de la aplicacion es:

```text
Base de datos: carescan_db
Usuario: root
Contrasena: 1234
Puerto MySQL: 3306
```

Si el equipo usa otra contrasena o usuario de MySQL, no hace falta editar el codigo. Antes de arrancar Java, definir variables de entorno en PowerShell:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/carescan_db?useSSL=false&serverTimezone=UTC"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "CONTRASENA_DE_MYSQL"
```

Las tablas se crean automaticamente al arrancar porque el proyecto usa:

```properties
spring.jpa.hibernate.ddl-auto=update
```

## 4. Arrancar la API Python del modelo

Abrir una terminal PowerShell en la carpeta del proyecto:

```powershell
cd RUTA\A\TFG\python-model-api
```

Crear el entorno virtual. En Windows se recomienda una ruta corta para evitar errores de rutas largas con TensorFlow:

```powershell
python -m venv C:\bta-venv
```

Si el comando `python` no apunta al Python oficial, usar la ruta completa. Ejemplo:

```powershell
C:\Users\USUARIO\AppData\Local\Programs\Python\Python313\python.exe -m venv C:\bta-venv
```

Instalar dependencias:

```powershell
C:\bta-venv\Scripts\python.exe -m pip install --upgrade pip
C:\bta-venv\Scripts\python.exe -m pip install -r requirements.txt
```

Arrancar la API:

```powershell
$env:BRAIN_TUMOR_MODEL_PATH = ".\models\model-32-0.99-0.04.h5"
$env:BRAIN_TUMOR_MODEL_VERSION = "resnet50-brain-tumor-v1"
$env:BRAIN_TUMOR_LAST_CONV_LAYER = "conv5_block3_out"
C:\bta-venv\Scripts\python.exe -m uvicorn app:app --host 127.0.0.1 --port 8000
```

Dejar esta terminal abierta.

Para comprobar que la API esta funcionando, abrir otra terminal y ejecutar:

```powershell
Invoke-RestMethod -Uri http://127.0.0.1:8000/health
```

Debe devolver algo similar a:

```text
status = ok
modelPath = .\models\model-32-0.99-0.04.h5
lastConvLayer = conv5_block3_out
```

## 5. Arrancar la aplicacion Java

Abrir otra terminal PowerShell en la carpeta raiz del proyecto:

```powershell
cd RUTA\A\TFG
```

Si MySQL no usa `root / 1234`, definir las variables indicadas en el apartado 3.

Arrancar la aplicacion:

```powershell
mvn spring-boot:run
```

La aplicacion queda disponible en:

```text
https://localhost:8443
```

El certificado HTTPS es local/autofirmado. El navegador puede mostrar un aviso de seguridad; para la prueba local hay que aceptar la excepcion y continuar.

## 6. Usuarios iniciales

La aplicacion crea usuarios de prueba automaticamente si la base de datos esta vacia:

| Rol | Email | Contrasena |
| --- | --- | --- |
| Admin IT | `adminIT@carescan.com` | `1234adminit` |
| Admin hospital | `adminH@carescan.com` | `1234adminh` |
| Medico | `carla@carescan.com` | `1234carla` |

Flujo recomendado para probar:

1. Entrar como Admin hospital.
2. Crear un paciente.
3. Asignar el paciente al medico.
4. Entrar como medico.
5. Subir una imagen medica del paciente.
6. Ver resultado de clasificacion y mapa de calor.
7. Publicar el resultado al paciente.
8. Entrar como paciente y consultar resultados publicados.
9. Entrar como Admin IT y revisar usuarios y logs de auditoria.

## 7. Orden correcto de arranque

El orden recomendado es:

1. MySQL iniciado.
2. Base de datos `carescan_db` creada.
3. API Python arrancada en `http://127.0.0.1:8000`.
4. Aplicacion Java arrancada en `https://localhost:8443`.

Si la API Python no esta arrancada, la aplicacion Java puede abrir, pero el analisis por IA no estara disponible correctamente.

## 8. Problemas frecuentes

### Error de conexion a MySQL

Comprobar que MySQL esta iniciado, que existe la base de datos `carescan_db` y que las variables `DB_USERNAME` y `DB_PASSWORD` coinciden con el usuario local.

### TensorFlow no se instala

Usar Python oficial de Windows, no Python de MSYS2/MinGW. Comprobarlo con:

```powershell
where.exe python
python -c "import sys; print(sys.executable)"
```

### Error por rutas largas al instalar TensorFlow

Crear el entorno virtual en una ruta corta:

```powershell
python -m venv C:\bta-venv
```

### El navegador avisa de certificado no seguro

Es normal en ejecucion local porque el certificado incluido es autofirmado. Continuar a `https://localhost:8443`.

### La prediccion no funciona

Comprobar primero:

```powershell
Invoke-RestMethod -Uri http://127.0.0.1:8000/health
```

Si no responde, la API Python no esta arrancada o no encuentra el modelo.

## 9. Comandos resumidos

Terminal 1, API Python:

```powershell
cd RUTA\A\TFG\python-model-api
C:\bta-venv\Scripts\python.exe -m pip install -r requirements.txt
$env:BRAIN_TUMOR_MODEL_PATH = ".\models\model-32-0.99-0.04.h5"
$env:BRAIN_TUMOR_MODEL_VERSION = "resnet50-brain-tumor-v1"
$env:BRAIN_TUMOR_LAST_CONV_LAYER = "conv5_block3_out"
C:\bta-venv\Scripts\python.exe -m uvicorn app:app --host 127.0.0.1 --port 8000
```

Terminal 2, aplicacion Java:

```powershell
cd RUTA\A\TFG
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "CONTRASENA_DE_MYSQL"
mvn spring-boot:run
```

Abrir:

```text
https://localhost:8443
```



cd C:\Users\marta\Documents\DOBLE_GRADO\4_carrera\TFG\WEB\TFG\python-model-api

$env:BRAIN_TUMOR_MODEL_PATH = ".\models\model-32-0.99-0.04.h5"
$env:BRAIN_TUMOR_MODEL_VERSION = "resnet50-brain-tumor-v1"
$env:BRAIN_TUMOR_LAST_CONV_LAYER = "conv5_block3_out"

C:\Users\marta\bta-venv312\Scripts\python.exe -m uvicorn app:app --host 127.0.0.1 --port 8000