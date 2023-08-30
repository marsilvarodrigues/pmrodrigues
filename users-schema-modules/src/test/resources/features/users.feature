Feature: Management Users

  Scenario Outline: Create a new User on system
    Given An "admin" user
    When Create a new user with email <email> firstName <firstName> and lastName <lastName>
    Then User has a <propertyName> defined
    Examples:
      | email                | firstName  | lastName | propertyName |
      | "to_insert@test.com" | "new_user" | "insert" | "id" |

  Scenario Outline: Update a user on system
    Given An "admin" user
    And Create a new user with email <email> firstName <firstName> and lastName <lastName>
    When Update <propertyName> to <change_value>
    Then Check if statusCode is 200
    Examples:
      | email                | firstName   | lastName     | propertyName | change_value   |
      | "to_update@test.com" | "to_update" | "to_update"  | "firstName"  | "change_value" |

  Scenario Outline: Search user by field
    Given the following users
        | email              | firstName                      | lastName       |
        | to_get@test.com    | to_get_firstName               | to_get         |
        | to_update@test.com | to_update_firstName            | to_update      |
        | to_insert@test.com | new_user_firstName             | insert         |
    And An "admin" user
    When I filter by <propertyName> as <value>
    Then returned users list as
        | email                | firstName              | lastName         |
        | to_get@test.com      | to_get_firstName       | to_get           |
    Examples:
      | propertyName                  | value              |
      | "email"                       | "to_get@test.com"  |
      | "firstName"                   | "to_get_firstName" |
      | "lastName"                    | "to_get"           |

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

  Scenario: Delete User
    Given the following users
      | email              | firstName                      | lastName       |
      | to_get@test.com    | to_get_firstName               | to_get         |
      | to_update@test.com | to_update_firstName            | to_update      |
      | to_insert@test.com | new_user_firstName             | insert         |
    And An "admin" user
    And Id by "email" of "to_get@test.com"
    When Delete user
    Then returned users list as
      | email                | firstName                      | lastName       |
      | to_insert@test.com   | new_user_firstName             | insert         |
      | to_update@test.com   | to_update_firstName            | to_update      |

  Scenario Outline: Get User By Id
    Given the following users
      | email              | firstName                      | lastName       |
      | to_get@test.com    | to_get_firstName               | to_get         |
      | to_update@test.com | to_update_firstName            | to_update      |
      | to_insert@test.com | new_user_firstName             | insert         |
    And An "admin" user
    When Id by "email" of "to_get@test.com"
    Then User has "<propertyName>" equals to "<value>"
    Examples:
      | propertyName                | value                        |
      | email                       | to_get@test.com              |
      | firstName                   | to_get_firstName             |
      | lastName                    | to_get                       |
