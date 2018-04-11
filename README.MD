# Heat-Map-Trip #

* Completely independent service with UI interface
* Tourist sphere analyze helper

## Technologies ## 

- [x] Java 8
- [x] Spring Framework
    - [x] Spring Boot
    - [x] Spring Boot Property Configuration with *.yml
    - [x] Spring MVC project structure best practices
- [x] Gradle build tool
- [x] API visualization based on Swagger framework
- [x] Lombok plugin (@Get / @Set / @Builder / @SneakyThrows / @Slf4j)
- [x] PostgreSQL
    - [x] HikariCP   
- [x] Bower WEB build tool
- [x] WebComponents
- [x] Material design
- [x] Google Polymer
     
- [ ] Monitoring (*Glowroot*)
    - [ ] Rest calls
    - [ ] Heap
    - [ ] CPU
    - [ ] DB 
   
        
## Notes ##
Строим полный граф на всех объектах, суммируем расстояния, суммируем все рейтинги, берем среднее расстояние и делим. 

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
