# Phase 6: Meal Logging

## Goal
Allow users to log meals and food items by quantity and weight.

## Scope
1. Build meal logging screen.
2. Allow user to add:
   - date
   - mealType: breakfast, lunch, dinner, snack, drink
   - foodName
   - quantity
   - unit
   - weightGrams optional but recommended
3. Allow multiple food items per meal.
4. Store meal logs at `users/{uid}/mealLogs/{mealId}`.
5. Add edit and delete meal support.
6. Show today's meals on Home dashboard.
7. Add simple local nutrition estimate placeholders if no full nutrition database exists yet.
8. Keep the data model compatible with future nutrient values:
   - calories
   - proteinGrams
   - fiberGrams
   - folateMcg
   - ironMg
   - calciumMg
   - vitaminDMcg
   - iodineMcg
   - omega3Mg
   - cholineMg
9. Do not rely only on AI for nutrient calculations.
10. Add basic validation.

## Acceptance criteria
- User can add/edit/delete meal logs.
- Meals can contain multiple food items.
- Data model supports future nutrient calculation.

## Required commit message
`Phase 6: Add meal logging`
