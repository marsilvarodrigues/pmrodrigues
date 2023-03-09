Feature: Management Addresses

  Scenario Outline: Create my address
    Given a new user as "<email>" , "<firstName>" and "<lastName>"
    When I save my address as <state>, <addressType>, "<address>", "<zipcode>", "<neightboor>", "<city>"
    Then Address has a "<propertyName>" defined
    And Owner is the who logged
    Examples:
    | email         | firstName | lastName | state | addressType | address | zipcode   | neightboor | city  | propertyName |
    | test@test.com | test      | test     | RJ    | STREET      | teste   | 22222-222 | teste      | teste | id           |

  Scenario Outline: Update my address
    Given a new user as "<email>" , "<firstName>" and "<lastName>"
    And I save my address as <state>, <addressType>, "<address>", "<zipcode>", "<neightboor>", "<city>"
    When I change "<propertyName>" to "<newValue>"
    Then Address with "<propertyName>" is equals to "<newValue>"
    Examples:
      | email         | firstName | lastName | state | addressType | address | zipcode   | neightboor | city  | propertyName   | newValue |
      | test@test.com | test      | test     | RJ    | STREET      | teste   | 22222-222 | teste      | teste | city           | XPTO     |
      | test@test.com | test      | test     | RJ    | STREET      | teste   | 22222-222 | teste      | teste | address        | XPTO     |

  Scenario Outline: Delete my Address
    Given a new user as "<email>" , "<firstName>" and "<lastName>"
    And I save my address as <state>, <addressType>, "<address>", "<zipcode>", "<neightboor>", "<city>"
    When I delete my address
    Then My Address needs to be empty
    Examples:
      | email         | firstName | lastName | state | addressType | address | zipcode   | neightboor | city  |
      | test@test.com | test      | test     | RJ    | STREET      | teste   | 22222-222 | teste      | teste |

  Scenario: List my users
    Given a new user as "test@test.com" , "test" and "test"
    And a list of address as
      | state | addressType | address | zipcode   | neightboor | city  |
      | RJ    | STREET      | teste   | 22222-222 | teste      | teste |
      | AC    | STREET      | teste   | 11111-111 | teste      | teste |
      | SP    | STREET      | teste   | 33333-333 | teste      | teste |
      | RS    | STREET      | teste   | 44444-444 | teste      | teste |
      | RN    | STREET      | teste   | 55555-555 | teste      | teste |
    When I ask to list my address will return
    Then my address list is
      | state | addressType | address | zipcode   | neightboor | city  |
      | RJ    | STREET      | teste   | 22222-222 | teste      | teste |
      | AC    | STREET      | teste   | 11111-111 | teste      | teste |
      | SP    | STREET      | teste   | 33333-333 | teste      | teste |
      | RS    | STREET      | teste   | 44444-444 | teste      | teste |
      | RN    | STREET      | teste   | 55555-555 | teste      | teste |


