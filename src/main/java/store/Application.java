package store;

import store.controller.StoreController;
import store.service.OrderService;
import store.service.ProductService;
import store.util.FileReader;
import store.view.InputView;
import store.view.OutputView;

public class Application {
    public static void main(String[] args) {
        FileReader fileReader = new FileReader();
        ProductService productService = new ProductService(fileReader);
        OrderService orderService = new OrderService(productService);
        InputView inputView = new InputView();
        OutputView outputView = new OutputView();

        StoreController storeController = new StoreController(
                productService,
                orderService,
                inputView,
                outputView
        );

        storeController.run();
    }
}