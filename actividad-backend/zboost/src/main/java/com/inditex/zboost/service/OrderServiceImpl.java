package com.inditex.zboost.service;

import com.inditex.zboost.entity.Order;
import com.inditex.zboost.entity.OrderDetail;
import com.inditex.zboost.entity.ProductOrderItem;
import com.inditex.zboost.exception.InvalidParameterException;
import com.inditex.zboost.exception.NotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    private NamedParameterJdbcTemplate jdbcTemplate;

    public OrderServiceImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Order> findOrders(int limit) {
        /**
         * TODO: EJERCICIO 2.a) Recupera un listado de los ultimos N pedidos (recuerda ordenar por fecha)
         */
        if(limit < 1 || limit > 100){
            throw new InvalidParameterException(String.valueOf(limit), "Limit must be between 1 and 100");
        }
        Map<String, Object> params = new HashMap<>(limit);
        params.put("limit", limit);

        String sql = "SELECT ID, DATE, STATUS FROM ORDERS ORDER BY DATE DESC LIMIT :limit";

        return jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(Order.class));
    }

    @Override
    public List<Order> findOrdersBetweenDates(Date fromDate, Date toDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", new java.sql.Date(fromDate.getTime()));
        params.put("toDate", new java.sql.Date(toDate.getTime()));
        String sql = """
                SELECT id, date, status
                FROM Orders 
                WHERE date BETWEEN :startDate AND :toDate
                """;

        return jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(Order.class));
    }

    @Override
    public OrderDetail findOrderDetail(long orderId) {
        /**
         * TODO: EJERCICIO 2.b) Recupera los detalles de un pedido dado su ID
         *
         * Recuerda que, si un pedido no es encontrado por su ID, debes notificarlo debidamente como se recoge en el contrato
         * que estas implementando (codigo de estado HTTP 404 Not Found). Para ello puedes usar la excepcion {@link com.inditex.zboost.exception.NotFoundException}
         *
         */

        // Escribe la query para recuperar la entidad OrderDetail por ID
        String sql = "SELECT o.ID, o.DATE, o.STATUS, SUM(p.PRICE*oi.QUANTITY) AS \"totalPrice\", SUM(oi.QUANTITY) AS \"itemsCount\" FROM ORDERS o JOIN ORDER_ITEMS  oi ON o.ID = oi.ORDER_ID JOIN PRODUCTS p on oi.PRODUCT_ID=p.ID WHERE oi.ORDER_ID = :orderId GROUP BY o.ID, o.DATE, o.STATUS";
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        try {
            OrderDetail orderDetail = jdbcTemplate.queryForObject(sql, params, new BeanPropertyRowMapper<>(OrderDetail.class));

            // Una vez has conseguido recuperar los detalles del pedido, faltaria recuperar los productos que forman parte de el...
            String productOrdersSql = "SELECT p.ID, p.NAME, p.PRICE, p.CATEGORY, p.IMAGE_URL, oi.QUANTITY FROM PRODUCTS p JOIN ORDER_ITEMS oi ON p.ID = oi.PRODUCT_ID WHERE oi.ORDER_ID = :orderId";
            List<ProductOrderItem> products = jdbcTemplate.query(productOrdersSql, params, new BeanPropertyRowMapper<>(ProductOrderItem.class));
            if(!products.isEmpty())
                orderDetail.setProducts(products);
            else {
                orderDetail.setProducts(List.of());
            }
            return orderDetail;
        } catch (EmptyResultDataAccessException e){
            throw new NotFoundException(String.valueOf(orderId), "Order id not found");
        }
    }
}
