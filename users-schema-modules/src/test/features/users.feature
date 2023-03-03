Feature: Management Users

  Scenario Outline: Create a new User on system
    Given An "admin" user
    When Create a new user with email <email> firstName <firstName> and lastName <lastName>
    Then User has a <propertyName> defined
    Examples:
      | email                | firstName  | lastName | propertyName |
      | "to_insert@test.com" | "new_user" | "insert" | "externalId" |

  Scenario Outline: Update a user on system
    Given An "admin" user
    And Create a new user with email <email> firstName <firstName> and lastName <lastName>
    When Update <propertyName> to <change_value>
    Then Check if statusCode is 200
    Examples:
      | email                | firstName   | lastName     | propertyName | change_value   |
      | "to_update@test.com" | "to_update" | "to_update"  | "firstName"  | "change_value" |

  Scenario: Search user by email
    Given the following users
        | email              | firstName                      | lastName       |
        | to_get@test.com    | to_get_firstName               | to_get         |
        | to_update@test.com | to_update_firstName            | to_update      |
        | to_insert@test.com | new_user_firstName             | insert         |
    And An "admin" user
    When I filter by "email" as "to_get@test.com"
    Then returned users list as
        | email                | firstName              | lastName         |
        | to_get@test.com      | to_get_firstName       | to_get           |

  Scenario: Search user by firstName
    Given the following users
      | email              | firstName                      | lastName       |
      | to_get@test.com    | to_get_firstName               | to_get         |
      | to_update@test.com | to_update_firstName            | to_update      |
      | to_insert@test.com | new_user_firstName             | insert         |
    And An "admin" user
    When I filter by "firstName" as "to_get"
    Then returned users list as
      | email                | firstName              | lastName         |
      | to_get@test.com      | to_get_firstName       | to_get           |

  Scenario: Search user by lastName
    Given the following users
      | email              | firstName                      | lastName       |
      | to_get@test.com    | to_get_firstName               | to_get         |
      | to_update@test.com | to_update_firstName            | to_update      |
      | to_insert@test.com | new_user_firstName             | insert         |
    And An "admin" user
    When I filter by "lastName" as "to_get"
    Then returned users list as
      | email                | firstName              | lastName         |
      | to_get@test.com      | to_get_firstName       | to_get           |

  Scenario: List all user
    Given the following users
      | email              | firstName                      | lastName       |
      | to_get@test.com    | to_get_firstName               | to_get         |
      | to_update@test.com | to_update_firstName            | to_update      |
      | to_insert@test.com | new_user_firstName             | insert         |
    And An "admin" user
    When List all users
    Then returned users list as
      | email                | firstName                      | lastName       |
      | to_insert@test.com   | new_user_firstName             | insert         |
      | to_get@test.com      | to_get_firstName               | to_get         |
      | to_update@test.com   | to_update_firstName            | to_update      |
