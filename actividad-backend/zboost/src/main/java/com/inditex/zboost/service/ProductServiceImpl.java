package com.inditex.zboost.service;

import com.inditex.zboost.entity.Product;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductServiceImpl implements ProductService {

    private NamedParameterJdbcTemplate jdbcTemplate;

    public ProductServiceImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Product> findProducts(Optional<List<String>> categories) {
        /**
         * TODO: EJERCICIO 1.a) Utiliza el jdbcTemplate para recuperar productos por sus categorias. Si dicho filtro
         * no esta presente, recupera TODOS los productos del catalogo.
         *
         * Recuerda que el filtrado de categorias debe ser CASE-INSENSITIVE: la busqueda debe devolver los mismos resultados
         * filtrando por 'dresses', 'Dresses' o 'dRessES', por ejemplo.
         *
         * Para realizar filtrados en la clausula WHERE, recuerda que no es buena practica hacer un append directo de los
         * valores, si no que debes hacer uso de PreparedStatements para prevenir inyecciones de SQL. Ejemplo:
         *
         * "WHERE name = " + person.getName() + " AND ..." ==> MAL
         * "WHERE name = :name AND ..." ==> BIEN
         * 
         *  Pista: A la hora de filtrar, pasar los valores a mayúsculas o minúsculas. Ejemplo: Uso de la función SQL upper().
         */

        Map<String, Object> params = new HashMap<>();

        StringBuilder sql = new StringBuilder("SELECT ID, NAME, PRICE, CATEGORY, IMAGE_URL FROM Products");

        if(categories.isPresent() && categories.get().size()>0) {
            sql.append(" WHERE upper(CATEGORY) IN (");
            for(String category : categories.get()){
                if (Objects.equals(category, categories.get().get(categories.get().size() - 1))) {
                    sql.append("upper('").append(category).append("'))");
                } else {
                    sql.append("upper('").append(category).append("'),");
                }
            }
        }

        return jdbcTemplate.query(sql.toString(), params, new BeanPropertyRowMapper<>(Product.class));
    }

    @Override
    public List<String> findProductCategories() {
        /**
         * TODO: EJERCICIO 1.b) Recupera las distintas categorias de los productos disponibles.
         */

        String sql = "SELECT distinct(CATEGORY) FROM Products";

        return jdbcTemplate.queryForList(sql, (SqlParameterSource) null, String.class);
    }


}
