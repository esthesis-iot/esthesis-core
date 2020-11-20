# esthes.is - Backend server

## Start the application
With debugger on port 55000:
```
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=55000"
```

With jRebel support and debugger on port 55000:
```
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentpath:/JREBELPATH -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=55000"
```
