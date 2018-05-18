# Heat-Map-Trip #

* Completely independent service with UI interface
* Tourist sphere analyze helper

## Usage ## 

- Clone project 
- Go to src/main/resources/static
    - Run ```bower install```
- Configure PostgreSQL database connection according to application.yml from stc/main/resources
    ```
    url: jdbc:postgresql://localhost:5432/heat-map-trip
    username: postgres
    password: postgres
   ```
   Or update application.yml
- Run from root ```gradle bootRun``` 
- Ready! Please visit ```localhost:8080``` and take enjoy :)         

## Technologies ## 

- [x] Java 8
- [x] Spring Framework
    - [x] Spring Boot
    - [x] Spring Boot Property Configuration with *.yml
    - [x] Spring MVC project structure best practices
- [x] Gradle build tool
- [x] Swagger
- [x] Lombok plugin (@Get / @Set / @Builder / @SneakyThrows / @Slf4j)
- [x] PostgreSQL
    - [ ] HikariCP   
- [x] Bower WEB build tool
- [x] WebComponents
- [x] Material design
- [x] Google Polymer
- [x] Monitoring (*Glowroot*)
    - [ ] Rest calls
    - [x] Heap
    - [x] CPU
    - [ ] DB 
   
        
## Notes ##
- Строим полный граф на всех объектах, суммируем расстояния, суммируем все рейтинги, берем среднее расстояние и делим. 

## Technical usage ## 
1. Description about installation and bla-bla-bla         

## TODO ## 
- [x] Create categories based on Google / Foursquare API
    - [x] Migrate categories to properties (application.yml)
    - [x] Remove hardcoded categories on UI side   
- [x] Smart rest calls
    - [x] Service does small pauses after unsuccessfully calls and try repeat call 
- [x] 2 data source supporting
    - [x] Introduce several abstraction layers (OOP)
- [x] API visualization based on Swagger framework    
- [x] Migrate to jQuery functionality interface
- [x] Add spinner and disabled feature 
- [ ] Another features... 
- [ ] ... 