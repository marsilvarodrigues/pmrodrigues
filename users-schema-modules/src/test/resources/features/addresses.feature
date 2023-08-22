Feature: Management Addresses

  Scenario Outline: Create my address
    Given a new user as "<email>" , "<firstName>" and "<lastName>"
    When I save my address as <state>, <addressType>, "<address>", "<zipcode>", "<neighbor>", "<city>"
    Then Address has a "<propertyName>" defined
    And Owner is the who logged
    Examples:
    | email                 | firstName | lastName | state | addressType | address | zipcode   | neighbor   | city  | propertyName |
    | test@test_address.com | test      | test     | RJ    | STREET      | teste   | 22222-222 | teste      | teste | id           |

  Scenario Outline: Update my address
    Given a new user as "<email>" , "<firstName>" and "<lastName>"
    And I save my address as <state>, <addressType>, "<address>", "<zipcode>", "<neighbor>", "<city>"
    When I change "<propertyName>" to "<newValue>"
    Then Address with "<propertyName>" is equals to "<newValue>"
    Examples:
      | email                 | firstName | lastName | state | addressType | address | zipcode   | neighbor   | city  | propertyName   | newValue |
      | test@test_address.com | test      | test     | RJ    | STREET      | teste   | 22222-222 | teste      | teste | city           | XPTO     |
      | test@test_address.com | test      | test     | RJ    | STREET      | teste   | 22222-222 | teste      | teste | address1       | XPTO     |

  Scenario Outline: Delete my Address
    Given a new user as "<email>" , "<firstName>" and "<lastName>"
    And I save my address as <state>, <addressType>, "<address>", "<zipcode>", "<neighbor>", "<city>"
    When I delete my address
    Then My Address needs to be empty
    Examples:
      | email                 | firstName | lastName | state | addressType | address | zipcode   | neighbor   | city  |
      | test@test_address.com | test      | test     | RJ    | STREET      | teste   | 22222-222 | teste      | teste |

  Scenario: List my users
    Given a new user as "test@test_address.com" , "test" and "test"
    And a list of address as
      | state | addressType | address | zipcode   | neighbor | city  |
      | RJ    | STREET      | teste   | 22222-222 | teste      | teste |
      | AC    | STREET      | teste   | 11111-111 | teste      | teste |
      | SP    | STREET      | teste   | 33333-333 | teste      | teste |
      | RS    | STREET      | teste   | 44444-444 | teste      | teste |
      | RN    | STREET      | teste   | 55555-555 | teste      | teste |
    When I ask to list my address will return
    Then my address list is
      | state | addressType | address | zipcode   | neighbor | city    |
      | RJ    | STREET      | teste   | 22222-222 | teste      | teste |
      | AC    | STREET      | teste   | 11111-111 | teste      | teste |
      | SP    | STREET      | teste   | 33333-333 | teste      | teste |
      | RS    | STREET      | teste   | 44444-444 | teste      | teste |
      | RN    | STREET      | teste   | 55555-555 | teste      | teste |

  Scenario: Search by state from my address list
    Given a new user as "test@test_address.com" , "test" and "test"
    And a list of address as
      | state | addressType | address | zipcode   | neighbor | city             |
      | RJ    | STREET      | teste   | 22222-222 | teste      | Rio de Janeiro |
      | AC    | AVENUE      | teste   | 11111-111 | teste      | Rio Branco     |
      | SP    | ROAD        | teste   | 33333-333 | teste      | São Paulo      |
      | RS    | SQUARE      | teste   | 44444-444 | teste      | Porto Alegre   |
      | RN    | STREET      | teste   | 55555-555 | teste      | Natal          |
    When I search by state RJ
    Then my address list is
      | state | addressType | address | zipcode   | neighbor | city             |
      | RJ    | STREET      | teste   | 22222-222 | teste      | Rio de Janeiro |

  Scenario: Search by addressType from my address list
    Given a new user as "test@test_address.com" , "test" and "test"
    And a list of address as
      | state | addressType | address | zipcode   | neighbor | city             |
      | RJ    | STREET      | teste   | 22222-222 | teste      | Rio de Janeiro |
      | AC    | AVENUE      | teste   | 11111-111 | teste      | Rio Branco     |
      | SP    | ROAD        | teste   | 33333-333 | teste      | São Paulo      |
      | RS    | SQUARE      | teste   | 44444-444 | teste      | Porto Alegre   |
      | RN    | STREET      | teste   | 55555-555 | teste      | Natal          |
    When I search by addressType STREET
    Then my address list is
      | state | addressType | address | zipcode   | neighbor | city             |
      | RJ    | STREET      | teste   | 22222-222 | teste      | Rio de Janeiro |
      | RN    | STREET      | teste   | 55555-555 | teste      | Natal          |

  Scenario: Search by addressType from my address list
    Given a new user as "test@test_address.com" , "test" and "test"
    And a list of address as
      | state | addressType | address | zipcode   | neighbor | city             |
      | RJ    | STREET      | teste   | 22222-222 | teste      | Rio de Janeiro |
      | AC    | AVENUE      | teste   | 11111-111 | teste      | Rio Branco     |
      | SP    | ROAD        | teste   | 33333-333 | teste      | São Paulo      |
      | RS    | SQUARE      | teste   | 44444-444 | teste      | Porto Alegre   |
      | RN    | STREET      | teste   | 55555-555 | teste      | Natal          |
    When I search by city "Porto Alegre"
    Then my address list is
      | state | addressType | address | zipcode   | neighbor | city             |
      | RS    | SQUARE      | teste   | 44444-444 | teste      | Porto Alegre   |

