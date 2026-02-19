package api;

import api.clients.OrderClient;
import api.models.Order;
import api.utils.RandomUtils;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import static api.utils.RandomUtils.*;
import static org.hamcrest.Matchers.notNullValue;
import static org.apache.http.HttpStatus.*;

@RunWith(Parameterized.class)
public class OrderTest {
    private final List<String> color;
    private OrderClient orderClient;
    private int trackId;

    public OrderTest(List<String> color) {
        this.color = color;
    }

    @Parameterized.Parameters(name = "Цвет самоката: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {Collections.singletonList("BLACK")},
                {Collections.singletonList("GREY")},
                {Arrays.asList("BLACK", "GREY")},
                {null}
        });
    }

    @After
    public void tearDown() {
        if (trackId != 0) {
            orderClient.cancelOrder(trackId);
        }
    }

    @Test
    @DisplayName("Создание заказа с разными цветами")
    @Description("Проверка создания заказа с различными комбинациями цветов самоката")
    public void orderCanBeCreatedWithDifferentColors() {
        orderClient = new OrderClient();
        Order order = new Order(
                "Иван",
                "Иванов",
                "ул. Пушкина, д. 11",
                "8",
                generateRandomPhone(),
                3,
                "2024-12-31",
                "Комментарий",
                color
        );
        Response response = orderClient.createOrder(order);
        response.then()
                .statusCode(SC_CREATED)
                .body("track", notNullValue());
        trackId = response.then().extract().path("track");
    }
}