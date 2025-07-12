package com.example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.BeforeTest

class WorkshopTest {

    @BeforeTest
    fun setup() {
        Repository.reset()
    }


    @Test
    fun `test getAllRecipes returns all recipes`() {
        // Arrange: (No specific arrange needed as setup() provides initial state)

        // Act: เรียกใช้ฟังก์ชัน
        val recipes = Repository.getAllRecipes()

        // Assert: ตรวจสอบผลลัพธ์
        assertNotNull(recipes)
        assertEquals(2, recipes.size, "Should return 2 initial recipes")
        assertEquals("Fried Egg", recipes[0].name)
        assertEquals("Chicken Soup", recipes[1].name)
    }

    @Test
    fun `test getRecipeById returns correct recipe`() {
        // Arrange: ID ของเมนูที่คาดหวัง
        val recipeId = 1 // ID ของ Fried Egg จาก setup

        // Act: เรียกใช้ฟังก์ชัน
        val recipe = Repository.getRecipeById(recipeId)

        // Assert: ตรวจสอบผลลัพธ์
        assertNotNull(recipe, "Recipe with ID 1 should exist")
        assertEquals(recipeId, recipe.id)
        assertEquals("Fried Egg", recipe.name)
        assertEquals(1, recipe.ingredients.size, "Fried Egg should have 1 ingredient initially")
        assertEquals("Egg", recipe.ingredients[0].name)
        assertEquals(1, recipe.ingredients[0].id, "Egg ingredient ID should be 1 within its recipe")
    }

    @Test
    fun `test getRecipeById returns null if not found`() {
        // Arrange: ตั้งค่า input และผลลัพธ์ที่คาดหวัง ไม่มีID นี้
        val nonExistentId = 999

        // Act: เรียกใช้ฟังก์ชัน
        val recipe = Repository.getRecipeById(nonExistentId)

        // Assert: ตรวจสอบผลลัพธ์
        assertNull(recipe, "Should return null for non-existent recipe ID")
    }

    @Test
    fun `test addRecipe successfully adds a new recipe`() {
        // Arrange: ข้อมูลสำหรับสูตรอาหารใหม่
        val newRecipeRequest = RecipesRequest(
            name = "Omelette",
            instructions = "Cook whisked eggs in a pan.",
            ingredients = listOf(
                IngredientsRequest(name = "Egg", quantity = 2.0, unit = "units"),
                IngredientsRequest(name = "Cheese", quantity = 50.0, unit = "g")
            )
        )

        // Act: เรียกใช้ฟังก์ชัน
        val addedRecipe = Repository.addRecipe(newRecipeRequest)

        // Assert: ตรวจสอบผลลัพธ์
        assertNotNull(addedRecipe)
        assertEquals("Omelette", addedRecipe.name)
        assertEquals(3, addedRecipe.id, "New recipe ID should be 3 (after initial 1, 2)") // Depends on nextRecipeId
        assertEquals(2, addedRecipe.ingredients.size)
        assertEquals("Egg", addedRecipe.ingredients[0].name)
        assertEquals(1, addedRecipe.ingredients[0].id, "First ingredient in new recipe should have ID 1")
        assertEquals("Cheese", addedRecipe.ingredients[1].name)
        assertEquals(2, addedRecipe.ingredients[1].id, "Second ingredient in new recipe should have ID 2")

        // ตรวจสอบว่าถูกเพิ่มเข้าไปใน Repository จริงๆ
        assertEquals(3, Repository.getAllRecipes().size, "Total recipes should now be 3")
        assertNotNull(Repository.getRecipeById(3), "Omelette should be retrievable by its ID")
    }

    @Test
    fun `test updateRecipe updates existing recipe`() {
        // Arrange: ID ของเมนูที่ต้องการอัปเดต และข้อมูลใหม่
        val recipeIdToUpdate = 1
        val updatedName = "Super Fried Egg"
        val updatedInstructions = "Fry egg perfectly with crispy edges."
        val updatedIngredients = listOf(
            IngredientsRequest(id = 1, name = "Large Egg", quantity = 1.0, unit = "unit"),
            IngredientsRequest(name = "Pepper", quantity = 0.5, unit = "tsp")
        )
        val updateRequest = RecipesRequest(
            name = updatedName,
            instructions = updatedInstructions,
            ingredients = updatedIngredients
        )

        // Act: เรียกใช้ฟังก์ชัน
        val updatedRecipe = Repository.updateRecipe(recipeIdToUpdate, updateRequest)

        // Assert: ตรวจสอบผลลัพธ์
        assertNotNull(updatedRecipe, "Updated recipe should not be null")
        assertEquals(recipeIdToUpdate, updatedRecipe.id)
        assertEquals(updatedName, updatedRecipe.name)
        assertEquals(updatedInstructions, updatedRecipe.instructions)
        assertEquals(2, updatedRecipe.ingredients.size, "Should have 2 ingredients now (1 updated, 1 new)")
        assertTrue(updatedRecipe.ingredients.any { it.name == "Large Egg" && it.quantity == 1.0 && it.id == 1 })
        assertTrue(updatedRecipe.ingredients.any { it.name == "Pepper" && it.quantity == 0.5 && it.id == 2 }, "New ingredient should get ID 2 within this recipe")

        // ตรวจสอบใน Repository โดยตรง
        val fetchedRecipe = Repository.getRecipeById(recipeIdToUpdate)
        assertEquals(updatedName, fetchedRecipe?.name)
    }

    @Test
    fun `test deleteRecipe successfully deletes a recipe`() {
        // Arrange: ID ของเมนูที่ต้องการลบ
        val recipeIdToDelete = 1

        // Act: เรียกใช้ฟังก์ชัน
        val isDeleted = Repository.deleteRecipe(recipeIdToDelete)

        // Assert: ตรวจสอบผลลัพธ์
        assertTrue(isDeleted, "Recipe with ID 1 should be deleted successfully")
        assertNull(Repository.getRecipeById(recipeIdToDelete), "Recipe should no longer exist in Repository")
        assertEquals(1, Repository.getAllRecipes().size, "Total recipes should now be 1 (Chicken Soup)")
    }

    @Test
    fun `test deleteRecipe returns false if not found`() {
        // Arrange: ID ที่ไม่มีอยู่
        val nonExistentId = 999

        // Act: เรียกใช้ฟังก์ชัน
        val isDeleted = Repository.deleteRecipe(nonExistentId)

        // Assert: ตรวจสอบผลลัพธ์
        assertFalse(isDeleted, "Should return false for non-existent recipe ID")
    }



    @Test
    fun `test addIngredientToRecipe adds a new ingredient`() {
        // Arrange: ID ของเมนู และข้อมูลส่วนผสมใหม่
        val recipeId = 1 // Fried Egg
        val newIngredientRequest = IngredientsRequest(name = "Black Pepper", quantity = 0.25, unit = "tsp")

        // Act: เรียกใช้ฟังก์ชัน
        val addedIngredient = Repository.addIngredientToRecipe(recipeId, newIngredientRequest)

        // Assert: ตรวจสอบผลลัพธ์
        assertNotNull(addedIngredient)
        assertEquals("Black Pepper", addedIngredient.name)
        assertEquals(2, addedIngredient.id, "New ingredient in recipe 1 should get ID 2 (after Egg ID 1)")

        // ตรวจสอบว่าส่วนผสมถูกเพิ่มในเมนูนั้น
        val recipe = Repository.getRecipeById(recipeId)
        assertNotNull(recipe)
        assertEquals(2, recipe.ingredients.size, "Fried Egg should now have 2 ingredients")
        assertTrue(recipe.ingredients.any { it.name == "Black Pepper" && it.id == 2 })
    }

    @Test
    fun `test updateIngredientInRecipe updates existing ingredient`() {
        // Arrange: ID ของเมนู, ID ส่วนผสม, และข้อมูลใหม่
        val recipeId = 2 // Chicken Soup
        val ingredientId = 1 // Chicken
        val updatedIngredientRequest = IngredientsRequest(name = "Organic Chicken", quantity = 750.0, unit = "g")

        // Act: เรียกใช้ฟังก์ชัน
        val updatedIngredient = Repository.updateIngredientInRecipe(recipeId, ingredientId, updatedIngredientRequest)

        // Assert: ตรวจสอบผลลัพธ์
        assertNotNull(updatedIngredient)
        assertEquals(ingredientId, updatedIngredient.id)
        assertEquals("Organic Chicken", updatedIngredient.name)
        assertEquals(750.0, updatedIngredient.quantity)


        // ตรวจสอบว่าส่วนผสมถูกอัปเดตในเมนูนั้น
        val recipe = Repository.getRecipeById(recipeId)
        assertNotNull(recipe)
        assertTrue(recipe.ingredients.any { it.name == "Organic Chicken" && it.quantity == 750.0 && it.id == 1 })
    }

    @Test
    fun `test deleteIngredientFromRecipe deletes an ingredient`() {
        // Arrange: ID ของเมนู และ ID ส่วนผสมที่ต้องการลบ
        val recipeId = 2 // Chicken Soup
        val ingredientId = 2 // Carrot

        // Act: เรียกใช้ฟังก์ชัน
        val isDeleted = Repository.deleteIngredientFromRecipe(recipeId, ingredientId)

        // Assert: ตรวจสอบผลลัพธ์
        assertTrue(isDeleted, "Carrot should be deleted from Chicken Soup")

        // ตรวจสอบว่าส่วนผสมถูกลบในเมนูนั้น
        val recipe = Repository.getRecipeById(recipeId)
        assertNotNull(recipe)
        assertEquals(1, recipe.ingredients.size, "Chicken Soup should now have 1 ingredient (Chicken)")
        assertFalse(recipe.ingredients.any { it.name == "Carrot" }, "Carrot should not be in the recipe anymore")
    }

    @Test
    fun `test deleteIngredientFromRecipe returns false if ingredient not found`() {
        // Arrange: ID ของเมนู และ ID ส่วนผสมที่ไม่มีอยู่
        val recipeId = 1 // Fried Egg
        val nonExistentIngredientId = 999

        // Act: เรียกใช้ฟังก์ชัน
        val isDeleted = Repository.deleteIngredientFromRecipe(recipeId, nonExistentIngredientId)

        // Assert: ตรวจสอบผลลัพธ์
        assertFalse(isDeleted, "Should return false if ingredient ID not found in recipe")
    }

    @Test
    fun `test searchRecipesByIngredientName finds correct recipes`() {


        // Act: ค้นหาด้วยคำว่า "Egg"
        val recipesWithEgg = Repository.searchRecipesByIngredientName("Egg")

        // Assert: ตรวจสอบผลลัพธ์
        assertEquals(1, recipesWithEgg.size, "Should find 1 recipe containing 'Egg'")
        assertEquals("Fried Egg", recipesWithEgg[0].name)
        assertTrue(recipesWithEgg[0].ingredients.any { it.name == "Egg" })

        // Act: ค้นหาด้วยคำว่า "chicken" (ไม่สนใจตัวพิมพ์เล็ก-ใหญ่)
        val recipesWithChicken = Repository.searchRecipesByIngredientName("chicken")

        // Assert: ตรวจสอบผลลัพธ์
        assertEquals(1, recipesWithChicken.size, "Should find 1 recipe containing 'chicken'")
        assertEquals("Chicken Soup", recipesWithChicken[0].name)
        assertTrue(recipesWithChicken[0].ingredients.any { it.name == "Chicken" })

        // Act: ค้นหาคำที่ไม่มีอยู่
        val recipesWithNonExistentIngredient = Repository.searchRecipesByIngredientName("vegetable")

        // Assert: ตรวจสอบผลลัพธ์
        assertTrue(recipesWithNonExistentIngredient.isEmpty(), "Should find no recipes for 'vegetable'")
    }
}