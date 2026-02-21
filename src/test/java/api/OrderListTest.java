package api;

import api.clients.OrderClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Test;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.apache.http.HttpStatus.*;

public class OrderListTest {
    @Test
    @DisplayName("Получение списка заказов")
    @Description("Проверка получения списка всех заказов")
    public void ordersListCanBeRetrieved() {
        OrderClient orderClient = new OrderClient();
        Response response = orderClient.getOrdersList();
        response.then()
                .statusCode(SC_OK)
                .body("orders", notNullValue())
                .body("orders", instanceOf(List.class))
                .body("orders.size()", greaterThanOrEqualTo(0));
    }
}