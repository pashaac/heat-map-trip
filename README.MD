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
- [ ] PostgreSQL
    - [ ] HikariCP   
- [x] Bower WEB build tool
- [x] WebComponents
- [x] Material design
- [x] Google Polymer
     
- [ ] Monitoring (*Glowroot*)
    - [ ] Rest calls
    - [ ] Heap
    - [ ] CPU
    - [ ] DB 
    
            
## Features ##
- Smart rest calls
    - Service does small pauses after unsuccessfully calls and try repeat call 
- 2 data source supporting
    - Introduce several abstraction layers (OOP)
-    
        
## Notes ##
Строим полный граф на всех объектах, суммируем расстояния, суммируем все рейтинги, берем среднее расстояние и делим. 

## Technical usage ## 
1. Description about installation and bla-bla-bla         
         
1. Need update _updateValue(e) function in paper-dropdown-menu-multi.html, at the end of function add next lines:   
```
if (this._value.length === 0) {
    this._value = undefined;
}
```

## TODO ## 
- [ ] Create categories based on Google / Foursquare API
    - [ ] Migrate categories to properties (application.yml)
    - [ ] Remove hardcoded categories on UI side   
    
