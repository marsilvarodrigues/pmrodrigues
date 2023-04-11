Feature: Management Roles

  Scenario: Add a user into a role
    Given An "admin" user
    And a new user as "add_to_admin_role@role.com" , "new_user" and "to_admin"
    When add "add_to_admin_role@role.com" to "admin" role
    Then Check if statusCode is 200
    And Check if role "admin" returns list with "add_to_admin_role@role.com"