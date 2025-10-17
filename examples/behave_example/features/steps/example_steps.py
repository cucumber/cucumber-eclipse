from behave import given, when, then


@given('a number {num:d}')
def step_given_number(context, num):
    """Set the initial number in the context"""
    context.number = num


@when('I increment the number by {inc:d}')
def step_when_increment(context, inc):
    """Increment the number stored in the context"""
    context.number += inc


@then('the result should be {expected:d}')
def step_then_result(context, expected):
    """Verify the final result"""
    assert context.number == expected, f"expected {expected} but got {context.number}"
