#language: en
#This Feature has no validator enabled in the feature file but in the project preferences!
Feature: Connection between DataTable Key and a specific Step Value


  Scenario: All Keys are related to a Step Value. Example 1  
    Given the animal "Cat"  
      | Key 		| Value |
      | Color 		| Black	|
      | Lifespan  	| 3 	|
      | Whiskers	| 24    |
    
	Then the food is "fish"


  Scenario: All Keys are related to a Step Value. Example 2 
  	Given the animal "Elephant"
      | Key 		| Value |
      | Color 		| Grey	|
      | Lifespan	| 70 	|
      | Trunk   	| 1.8   |  
      | Tusk   		| 1.3   |  
    
    Then the food is "leaves"


  Scenario: There are some unrelated Keys to a Step Value. This Keys are available for other Step Value   
  	Given the animal "Cat" 
      | Key 		| Value |
      | Color 		| Black	|
      | Lifespan  	| 3 	|
      | Whiskers	| 24    |
      | Trunk   	| 1.8   |
    
	Then the food is "fish"
  

  Scenario: There are some unrelated Keys to a Step Value. This Keys are not available for each Step Value  
  	Given the animal "Cat" 
      | Key 		| Value |
      | Color 		| Black	|
      | Lifespan  	| 3 	|
      | Whiskers	| 24    |
      | Wings   	| 2   	|
    
	Then the food is "fish" 

