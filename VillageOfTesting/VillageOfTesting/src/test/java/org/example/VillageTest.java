package org.example;

import org.example.objects.Building;
import org.example.objects.Project;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.example.objects.Worker;

import java.util.ArrayList;


public class VillageTest {

    private Village village;

    @Before
    public void setUp() {
        village = new Village();
    }

    @Test
    public void addWorkers_AndSimulateADayInTheVillage() {
        // Initial resource values
        int initialFood = village.getFood();
        int initialWood = village.getWood();
        int initialMetal = village.getMetal();
        int expectedFood = 12;
        int expectedWood = 1;
        int expectedMetal = 1;

        // Add workers of different types
        village.AddWorker("Amber", "farmer");
        village.AddWorker("Brian", "lumberjack");
        village.AddWorker("Kent", "miner");
        village.Day();

        // Check that workers are added correctly
        assertEquals(expectedFood, village.getFood());
        assertEquals(expectedMetal, village.getMetal());
        assertEquals(expectedWood, village.getWood());
        assertWorkerExists(village.getWorkers(), "Amber", "farmer");
        assertWorkerExists(village.getWorkers(), "Brian", "lumberjack");
        assertWorkerExists(village.getWorkers(), "Kent", "miner");

        // Verify that resources have increased correctly
        assertTrue(village.getFood() > initialFood);
        assertTrue(village.getWood() > initialWood);
        assertTrue(village.getMetal() > initialMetal);
    }

    // Helper method to check if a worker exists in the list
    private void assertWorkerExists(ArrayList<Worker> workers, String name, String occupation) {
        assertTrue("Worker with name " + name + " and job " + occupation + " should exist in the list",
                workers.stream().anyMatch(worker -> worker.getName().equals(name) && worker.getOccupation().equals(occupation)));
    }

    @Test
    public void cannotAddMore_WorkersThanMax() {
        // Given: Adds maximum number of workers.
        for (int i = 0; i < village.getMaxWorkers(); i++) {
            village.AddWorker("Worker" + i, "builder");
        }

        // When: Attempting to add one more worker beyond the maximum capacity.
        boolean wasAdded = village.AddWorker("ExtraWorker", "builder");

        // Then: Checks so the village is full and the extra worker was not added.
        assertTrue("Village should be full when maximum workers are added.", village.isFull());
        assertFalse("Should not be able to add more workers than max.", wasAdded);
    }

    @Test
    public void addHouseProject_WithSufficientResources() {
        // Set up village with sufficient resources for a House project
        village.setWood(5); // Assume House requires 5 units of wood
        village.setMetal(0); // Assume House requires 0 units of metal

        // Try to add the House project
        village.AddProject("House");

        // Verify that the House project has been successfully added
        assertFalse("Project list should not be empty after adding House", village.getProjects().isEmpty());
        assertEquals("First project should be House", "House", village.getProjects().get(0).getName());
        assertEquals("Remaining wood should be 0 after adding House", 0, village.getWood());
        assertEquals("Remaining metal should be unchanged", 0, village.getMetal());
    }

    @Test
    public void buildingHouse_IncreasesWorkerCapacity() {
        // Initial setup
        village.setWood(5); // Set enough wood for the "House" project
        village.setMetal(0); // Set metal for the "House" project
        village.AddWorker("Bob", "builder");
        village.AddProject("House");

        // Simulate the days required to build the house
        // Replace 3 with the actual number of days required if different
        village.Day(); // Day 1
        village.Day(); // Day 2
        village.Day(); // Day 3

        // Assertions
        int expectedMaxWorkers = 8; // 6 from initial houses + 2 from the new house
        assertEquals("Max workers should increase by 2 after building a house.", expectedMaxWorkers, village.getMaxWorkers());
    }

    @Test
    public void nextDayWithNoWorkers_ShouldNotAffectVillage() {
        // Arrange: Set the initial state of the village
        int initialFood = village.getFood();
        int initialWood = village.getWood();
        int initialMetal = village.getMetal();
        int initialNumberOfBuildings = village.getBuildings().size();
        int initialDaysGone = village.getDaysGone();

        // Ensure there are no workers
        village.setWorkers(new ArrayList<>());

        // Act: Advance to the next day
        village.Day();

        // Assert: Verify the state of the village has not changed
        assertEquals("Food should not change when there are no workers", initialFood, village.getFood());
        assertEquals("Wood should not change when there are no workers", initialWood, village.getWood());
        assertEquals("Metal should not change when there are no workers", initialMetal, village.getMetal());
        assertEquals("Number of buildings should not change when there are no workers", initialNumberOfBuildings, village.getBuildings().size());
        assertEquals("Days gone should increment by 1", initialDaysGone + 1, village.getDaysGone());
    }

    @Test
    public void nextDayWithWorkersAndNoFood_ShouldAffectWorkerHealth() {
        // Add workers of different types
        village.AddWorker("Amber", "farmer");
        village.AddWorker("Brian", "lumberjack");
        village.AddWorker("Kent", "miner");

        // Set food to 0, simulating no available food
        village.setFood(0);

        // Move to the next day
        village.Day();

        // Check that each worker is hungry
        assertTrue("Amber should be hungry", village.getWorkers().get(0).isHungry());
        assertTrue("Brian should be hungry", village.getWorkers().get(1).isHungry());
        assertTrue("Kent should be hungry", village.getWorkers().get(2).isHungry());
    }

    @Test
    public void lumberjack_CollectsWoodCorrectly() {
        // Preparation: Add a lumberjack and set initial resources
        int initialWood = village.getWood();
        village.AddWorker("Brian", "lumberjack");

        // Action: Simulate a day
        village.Day();

        // Verification: Check that the amount of wood has increased correctly
        int expectedWoodIncrease = 1; // According to game logic
        assertEquals("Wood should increase by the expected amount after a day with one lumberjack working", initialWood + expectedWoodIncrease, village.getWood());
    }

    @Test
    public void workersStarvation_AndGameOver() {
        // Add workers to the village
        String occupation = "miner";
        for (int i = 1; i <= village.getMaxWorkers(); i++) {
            village.AddWorker("Worker" + i, occupation);
        }
        // Initially set food to zero, simulating a lack of available food
        village.setFood(0);

        // Simulate the number of days without food for the workers
        // Use Worker.daysUntilStarvation if available.
        // Otherwise, use the known figure for your game scenario.
        int daysToStarve = 6; // Or Worker.daysUntilStarvation if available
        for (int i = 0; i < daysToStarve; i++) {
            village.Day(); // Advance to the next day, which will affect workers' health
        }

        // Verify there is no food left and all workers are dead
        assertEquals("There should be no food left", 0, village.getFood());
        for (Worker worker : village.getWorkers()) {
            assertFalse("All workers should be dead", worker.isAlive());
        }

        // Check that the game is over
        assertTrue("The game should be over", village.isGameOver());
    }

    @Test
    public void addProject_WithInsufficientResources() {
        // Ensure there are not enough resources to add a project
        village.setWood(2); // Less than the requirement for any project
        village.setMetal(0); // Assume the project requires more metal

        // Try to add a project, e.g., a Quarry
        village.AddProject("Quarry");

        // The project list should be empty as there were not enough resources
        assertTrue("Project list should be empty due to insufficient resources", village.getProjects().isEmpty());
    }

    @Test
    public void buildWoodmill_UntilItIsComplete_ShouldIncreaseWoodPerDayTo2() {
        // Setup to build a Woodmill
        village.setWood(10); // Sets initial resources sufficient to build a Woodmill
        village.setFood(10); // Initial food amount to ensure workers can eat
        village.setMetal(10); // Initial metal amount, even if not required for Woodmill, good to have for completeness of the test
        village.AddWorker("Levin", "builder"); // Adds a builder to work on the project

        // Add the Woodmill project
        village.AddProject("Woodmill");

        // Since the Build() method does not take any arguments and the building work is handled automatically in the Day() method,
        // we do not need to call the Build() method directly. Instead, we let the Day() method handle the building process.

        // Simulate the necessary days to complete the building
        // The number of days it takes to build a Woodmill should be defined in your game,
        // but we use 5 days here based on your previous example
        for (int i = 0; i < 5; i++) {
            village.Day(); // Each call represents a day in the game
        }

        // Check that Woodmill is built and added to the buildings list
        boolean isWoodmillBuilt = village.getBuildings().stream()
                .anyMatch(building -> "Woodmill".equals(building.getName()));
        assertTrue("Woodmill should be built and present in the buildings list", isWoodmillBuilt);

        // Verify that wood production has increased to 2 per day after Woodmill is completed
        assertEquals("Wood production per day should increase to 2 after building a Woodmill", 2, village.getWoodPerDay());
    }

    @Test
    public void minerCollects_MetalCorrectly() {
        // Preparation: Add a miner and set initial resources
        int initialMetal = village.getMetal();
        village.AddWorker("Caitlyn", "miner");

        // Action: Simulate a day
        village.Day();

        // Verification: Check that the amount of metal has increased correctly
        int expectedMetalIncrease = 1; // According to game logic
        assertEquals("Metal should increase by the expected amount after a day with one miner working", initialMetal + expectedMetalIncrease, village.getMetal());
    }

    @Test
    public void buildingFarm_IncreasesFoodProduction() {
        // Set up initial values and add a builder
        village.setWood(10); // Assume this is enough to build a Farm
        village.setMetal(5); // Assume this is enough to build a Farm
        village.AddWorker("Charlie", "builder");

        // Add and complete the building of a Farm
        village.AddProject("Farm");
        int daysToBuildFarm = 5; // Assume it takes 5 days to build a Farm
        for (int i = 0; i < daysToBuildFarm; i++) {
            village.Day();
        }

        // Check that the building is complete and that food production has increased
        assertTrue("Farm should be in the list of buildings", village.getBuildings().stream().anyMatch(b -> b.getName().equals("Farm")));
        int expectedFoodPerDay = 10; // Assume a Farm increases food production by 5
        assertEquals("Food production should increase after building a Farm", expectedFoodPerDay, village.getFoodPerDay());
    }

    @Test
    public void buildingQuarry_IncreasesMetalProduction() {
        // Set up initial values and add a builder
        village.setWood(3);
        village.setMetal(5);
        village.AddWorker("Dave", "builder");

        // Add and complete the building of a Quarry
        village.AddProject("Quarry");
        int daysToBuildQuarry = 7; // Assume it takes 7 days to build a Quarry
        for (int i = 0; i < daysToBuildQuarry; i++) {
            village.Day();
        }

        // Check that the building is complete and that metal production has increased
        assertTrue("Quarry should be in the list of buildings", village.getBuildings().stream().anyMatch(b -> b.getName().equals("Quarry")));
        int expectedMetalPerDay = 2; // Assume a Quarry increases metal production by 1
        assertEquals("Metal production should increase after building a Quarry", expectedMetalPerDay, village.getMetalPerDay());
    }

    @Test
    public void addProject() {
        // Arrange: Set resources enough to add a project
        village.setWood(10);
        village.setMetal(10);

        // Act: Add a project that can be afforded with current resources
        village.AddProject("House");

        // Assert: The project should be added successfully
        assertEquals("Should have one project", 1, village.getProjects().size());

        // Act: Try to add another project without sufficient resources
        village.setWood(0);
        village.setMetal(0);
        village.AddProject("Castle");

        // Assert: No additional projects should be added when resources are insufficient
        assertEquals("Should still have only one project due to insufficient resources", 1, village.getProjects().size());
    }

    @Test
    public void adding_InvalidProject() {
        // Given: A village with sufficient resources
        village.setWood(100);
        village.setMetal(100);

        // When: Attempting to add a project that does not exist
        village.AddProject("InvalidProject");

        // Then: No project should be added and the resources should remain the same
        assertEquals("Projects list should not change", 0, village.getProjects().size());
        assertEquals("Wood resource should not change", 100, village.getWood());
        assertEquals("Metal resource should not change", 100, village.getMetal());
    }

    @Test
    public void cannotAddProject_WhenInsufficientResources() {
        // Given: A village with insufficient resources
        village.setWood(10);
        village.setMetal(10);

        // When: Attempting to add a project
        int initialProjectSize = village.getProjects().size();
        village.AddProject("Castle");

        // Then: No project should be added
        assertEquals("No new projects should be added due to insufficient resources", initialProjectSize, village.getProjects().size());
    }

    @Test
    public void projectAddsNewBuilding_WhenCompleted() {
        // Given: A village with a builder and enough resources to start a project
        village.AddWorker("Builder Ben", "builder");
        village.setWood(100);
        village.setMetal(100);
        village.AddProject("House");

        // When: Builders work on the project until completion
        while (!village.getProjects().isEmpty()) {
            village.Day();
        }

        // Then: New building should be added to the village
        assertTrue("House should be added to buildings",
                village.getBuildings().stream().anyMatch(b -> b.getName().equals("House")));
    }

    @Test
    public void woodmillBuilding_CompletionAndEffect() {
        // Set up the resources sufficiently to initiate a Woodmill project
        village.setWood(5); // Assume this is sufficient
        village.setMetal(1); // Assume this is sufficient


        // Add the Woodmill project
        village.AddProject("Woodmill");

        // Add a Builder to construct the project
        village.AddWorker("Bob", "builder");

        // Simulate enough days to complete the construction of the Woodmill
        int buildDaysForWoodmill = 5; // Här anger vi antalet dagar det tar att bygga en Woodmill
        for (int i = 0; i < buildDaysForWoodmill; i++) {
            village.Day();
        }

        // Verify that the Woodmill is present in the list of buildings
        boolean isWoodmillBuilt = false;
        for (Building building : village.getBuildings()) {
            if ("Woodmill".equals(building.getName())) {
                isWoodmillBuilt = true;
                break;
            }
        }
        assertTrue("Woodmill should be built", isWoodmillBuilt);

        // Assume a Lumberjack collects 1 wood per day without a Woodmill
        int woodCollectedByOneLumberjack = 1;

        // Assume that a Woodmill increases wood collection by 1 wood per Lumberjack
        int additionalWoodAfterWoodmill = 1;

        // Add a Lumberjack
        village.AddWorker("Charlie", "lumberjack");

        // Simulate a day for the wood production to occur with the Woodmill effect
        int initialWood = village.getWood(); // Trä mängd före vedproduktion
        village.Day();

        // The expected amount of wood to be collected after a day with the Woodmill effect
        int expectedWood = initialWood + (woodCollectedByOneLumberjack + additionalWoodAfterWoodmill);
        assertEquals("Wood production should increase by 1 per lumberjack after building Woodmill",
                expectedWood,
                village.getWood());
    }

    @Test
    public void addingAndCompletingQuarry_IncreasesMetalProduction() {
        // Given: A village with sufficient resources to start and complete a "Quarry" project
        village.setWood(100); // Assuming 100 units of wood is sufficient
        village.setMetal(100); // Assuming 100 units of metal is sufficient
        village.AddProject("Quarry"); // Adding "Quarry" project
        village.AddWorker("Builder1", "builder"); // Adding builders to work on the project
        village.AddWorker("Builder2", "builder");

        // When: The "Quarry" project is completed
        while (!village.getProjects().isEmpty()) {
            village.Day(); // Simulating days until the project is completed
        }

        // Then: Verify "Quarry" is built and metal production has increased
        boolean isQuarryBuilt = village.getBuildings().stream()
                .anyMatch(building -> "Quarry".equals(building.getName()));
        assertTrue("Quarry should be built and present in the list of buildings", isQuarryBuilt);

        // Assuming initial metal production rate and expected increase after completing "Quarry"
        int expectedMetalProductionIncrease = 1; // Example value, adjust as necessary
        int actualMetalProduction = village.getMetalPerDay();
        assertTrue("Metal production should increase after completing 'Quarry'",
                actualMetalProduction > expectedMetalProductionIncrease);
    }

    @Test
    public void addingInvalidProject_NameDoesNothing() {
        // Attempt to add a project with a name that doesn't exist
        int initialProjectSize = village.getProjects().size();
        village.AddProject("InvalidProjectName");

        // Verify that no new project was added
        assertEquals(initialProjectSize, village.getProjects().size());
    }

    @Test
    public void addingWorker_WithInvalidOccupation() {
        // Attempt to add a worker with an invalid occupation
        boolean success = village.AddWorker("Bob", "dragon slayer");

        // Verify that the worker was not added
        assertFalse(success);
    }

    @Test
    public void dailyProject_Progress() {
        // Given: A village with sufficient resources to start a "Quarry" project
        village.setWood(100); // Assuming 100 units of wood is sufficient
        village.setMetal(100); // Assuming 100 units of metal is sufficient
        village.AddProject("Quarry"); // Adding "Quarry" project which takes multiple days to complete
        village.AddWorker("BuilderBob", "builder"); // Adding a builder to work on the project

        // Capture initial days left for "Quarry" project before simulation
        Project quarryProject = village.getProjects().get(0);
        int initialDaysLeft = quarryProject.getDaysLeft();

        // When: A day passes in the village
        village.Day();

        // Then: The number of days left to complete the "Quarry" project should decrease by one
        assertEquals("After a day's work, the number of days left on 'Quarry' project should decrease by one",
                initialDaysLeft - 1,
                quarryProject.getDaysLeft());
    }

    @Test
    public void projectCompletionEffects() {
        // Given: A village with a builder and enough resources to start and complete a Quarry project
        village.AddWorker("Builder Bob", "builder");
        village.setWood(10); // Adjust based on actual Quarry costs
        village.setMetal(10); // Adjust based on actual Quarry costs
        village.AddProject("Quarry");

        // When: Quarry project is completed
        while (!village.getProjects().isEmpty()) {
            village.Day();
        }

        // Then: Metal production should increase by Quarry's effect
        assertEquals("Metal production should increase after Quarry is completed", 2, village.getMetalPerDay());
    }

    @Test
    public void buildCastle_ToWinGames() {
        //Given (Setup resources for creating a castle)
        village.setWood(50);
        village.setFood(50);
        village.setMetal(50);
        String nameProject = "Castle";
        // When (Adding Projects and Workers)
        village.AddWorker("Micke", "builder");
        village.AddProject(nameProject);
        village.Build("Micke");
        for (int i = 1; i < 50; i++) { // Simulate the number of days to build a complete castle
            village.Day();
        }
        // Then
        Building building = village.getBuildings().get(3);
        assertEquals(49, village.getDaysGone());
        assertEquals(nameProject, building.getName());
        assertTrue(village.isGameOver());
    }

    @Test
    public void simulateGame_andWin() {
        addMaxWorker("Robert");
        String nameProject = "House";
        System.out.println();
        simulatesDay(3);

        village.AddProject(nameProject);
        village.Build("Robert5");
        simulatesDay(3);

        village.AddWorker("Martin1", "miner");
        village.AddWorker("Martin2", "miner");
        nameProject = "Woodmill";
        village.AddProject(nameProject);
        village.Build("Martin5");
        simulatesDay(5);

        nameProject = "Quarry";
        village.AddProject(nameProject);
        village.Build("Martin5");
        simulatesDay(6);

        nameProject = "Quarry";
        village.AddProject(nameProject);
        village.Build("Martin5");
        simulatesDay(6);

        nameProject = "Castle";
        village.AddProject(nameProject);
        village.Build("Martin5");
        simulatesDay(49);

        ArrayList<Worker> workers = village.getWorkers();
        Building building = village.getBuildings().get(7);

        assertEquals("Castle", building.getName());
        assertTrue(village.isGameOver());
        assertEquals(8, workers.size());
    }

    public void addMaxWorker(String name) {
        String[] occupation = {"farmer", "farmer", "lumberjack", "miner", "lumberjack", "builder"};
        for (int i = 0; i < occupation.length && i < village.getMaxWorkers(); i++) {
            village.AddWorker((name + i), occupation[i]);
        }
    }

    public void simulatesDay(int day) {
        for (int i = 0; i < day; i++) {
            village.Day();
            System.out.println("Day: " + village.getDaysGone());
            System.out.println("Current Wood: " + village.getWood());
            System.out.println("Current Food: " + village.getFood());
            System.out.println("Current Miner " + village.getMetal() + "\n");
        }
    }
}
















