import com.dunzo.coffeemachine.CoffeeMachine;
import com.dunzo.coffeemachine.bean.request.MachineInitializationRequest;
import com.dunzo.coffeemachine.entity.Beverage;
import com.dunzo.coffeemachine.entity.Item;
import com.dunzo.coffeemachine.exception.BeverageException;
import com.dunzo.coffeemachine.exception.ItemException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TestCoffeeMachineInputJson1 {

    CoffeeMachine coffeeMachine;
    MachineInitializationRequest request;

    @Before
    public void start() throws IOException {
        final String filePath = "input.json";
        File file = new File(CoffeeMachine.class.getClassLoader().getResource(filePath).getFile());
        String jsonInput = FileUtils.readFileToString(file, "UTF-8");
        this.request = new ObjectMapper().readValue(jsonInput,
                MachineInitializationRequest.class);
        this.coffeeMachine = CoffeeMachine.getInstance(jsonInput);
    }

    /**
     * Test case provided with the assignment, make all 4 drinks, 2 should fail, 1 insufficient items and 1
     * unavailable item
     */
    @Test
    public void providedTestCase() {
        List<CompletableFuture<Beverage>> futureList = Arrays.asList(
                coffeeMachine.getBeverageService().makeBeverage("hot_tea"),
                coffeeMachine.getBeverageService().makeBeverage("hot_coffee"),
                coffeeMachine.getBeverageService().makeBeverage("black_tea"),
                coffeeMachine.getBeverageService().makeBeverage("green_tea")
        );
        int insufficientCount = 0;
        int unavailableCount = 0;
        try {
            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
        } catch (Exception ignored) {
        }

        for (CompletableFuture<Beverage> future : futureList) {
            if (future.isCompletedExceptionally()) {
                try {
                    future.get();
                } catch (ExecutionException | InterruptedException e) {
                    if (e.getCause() instanceof BeverageException) {
                        BeverageException exp = (BeverageException) e.getCause();
                        if (exp.getMessage().contains("Insufficient Items for Beverage"))
                            insufficientCount++;
                    } else if (e.getCause() instanceof ItemException) {
                        ItemException exp = (ItemException) e.getCause();
                        if (exp.getMessage().equals("Invalid Item green_mixture"))
                            unavailableCount++;
                    }
                }
            }
        }
        Assert.assertEquals(1, insufficientCount);
        Assert.assertEquals(1, unavailableCount);
    }

    /**
     * Make many beverages to test if service throws proper exceptions for insufficient item quantity, used exception
     * strings for comparison, in ideal condition should have used error codes
     */
    @Test
    public void testForInSufficientItems() throws Exception {
        List<CompletableFuture<Beverage>> futureList = Arrays.asList(
                coffeeMachine.getBeverageService().makeBeverage("hot_tea"),
                coffeeMachine.getBeverageService().makeBeverage("hot_tea"),
                coffeeMachine.getBeverageService().makeBeverage("hot_tea"),
                coffeeMachine.getBeverageService().makeBeverage("hot_tea")
        );
        int insufficientCount = 0;
        try {
            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
        } catch (Exception ignored) {
        }

        for (CompletableFuture<Beverage> future : futureList) {
            if (future.isCompletedExceptionally()) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof BeverageException) {
                        BeverageException exp = (BeverageException) e.getCause();
                        if ("Insufficient Items for Beverage hot_tea".equals(exp.getMessage()))
                            insufficientCount++;
                    }
                }
            }
        }
        Assert.assertEquals(2, insufficientCount);
    }

    /**
     * Add item quantity to machine using item service addItem API and then test if item quantity is sufficient for
     * the beverages
     */
    @Test
    public void addItemsAndTestForSufficientItems() throws Exception {

        coffeeMachine.getItemService().addItem(new Item("hot_water", 300)).join();
        coffeeMachine.getItemService().addItem(new Item("tea_leaves_syrup", 20)).join();

        List<CompletableFuture<Beverage>> futureList = Arrays.asList(
                coffeeMachine.getBeverageService().makeBeverage("hot_tea"),
                coffeeMachine.getBeverageService().makeBeverage("hot_tea"),
                coffeeMachine.getBeverageService().makeBeverage("hot_tea"),
                coffeeMachine.getBeverageService().makeBeverage("hot_tea")
        );
        int insufficientCount = 0;
        try {
            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
        } catch (Exception ignored) {
        }

        for (CompletableFuture<Beverage> future : futureList) {
            if (future.isCompletedExceptionally()) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof BeverageException) {
                        BeverageException exp = (BeverageException) e.getCause();
                        if ("Insufficient Items for Beverage hot_tea".equals(exp.getMessage()))
                            insufficientCount++;
                    }
                }
            }
        }
        Assert.assertEquals(0, insufficientCount);
    }

    /**
     * After making a beverage check if item quantity is properly maintained or not
     */
    @Test
    public void testItemQuantityAfterTransaction() {
        coffeeMachine.getBeverageService().makeBeverage("hot_tea").join();
        List<Item> itemList = coffeeMachine.getItemService().getAllAvailableItems().join();
        for (Item item : itemList) {
            for (Map.Entry<String, Integer> testItem : request.getMachine().getBeverages().get("hot_tea").entrySet()) {
                if (testItem.getKey().equals(item.getName())) {
                    Integer testQuantity =
                            request.getMachine().getIngredientQuantityMap().get(testItem.getKey()) - testItem.getValue();
                    Assert.assertEquals(testQuantity, item.getQuantity());
                }
            }
        }
    }

    @After
    public void end() {
        this.coffeeMachine.reset();
    }
}
