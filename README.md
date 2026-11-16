# Router Gladys y Steven

Aplicación Android de diagnóstico local para el Huawei EchoLife EG8145V5.

## Primera versión
- Comprueba conectividad TCP en los puertos 80, 443, 8080 y 8443 de 192.168.18.1.
- Intenta leer la respuesta HTTP/HTTPS cuando un puerto responde.
- Presenta un informe para distinguir falta de conectividad de un servicio web accesible.
- No obtiene, adivina ni modifica contraseñas; no evita el inicio de sesión ni altera la configuración del operador.

## Compilar APK
Cada cambio en `main` ejecuta GitHub Actions. El APK de depuración se publica como artefacto llamado `Router-Gladys-y-Steven-debug`.
