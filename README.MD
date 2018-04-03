# Heat-Map-Trip #

* Completely independent service with UI interface
* Tourist sphere analyze helper

## Technologies / Features ## 

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
     
        
        - [] Monitoring (*Glowroot*)
            - [x] Rest calls
            - [] Heap
            - [] CPU
            - [] DB 
        
        
        Строим полный граф на всех объектах, суммируем расстояния, суммируем все рейтинги, берем среднее расстояние и делим. 
        
1. Need update _updateValue(e) function in paper-dropdown-menu-multi.html, at the end of function add next lines:   
```
if (this._value.length === 0) {
    this._value = undefined;
}
```
