from behave import given, when, then

class Calculator:
    def __init__(self):
        self.result = 0
    
    def add(self, a, b):
        self.result = a + b
        return self.result
    
    def subtract(self, a, b):
        self.result = a - b
        return self.result
    
    def multiply(self, a, b):
        self.result = a * b
        return self.result

@given('I have a calculator')
def step_impl(context):
    context.calculator = Calculator()

@when('I add {a:d} and {b:d}')
def step_impl(context, a, b):
    context.calculator.add(a, b)

@when('I subtract {a:d} from {b:d}')
def step_impl(context, a, b):
    context.calculator.subtract(b, a)

@when('I multiply {a:d} by {b:d}')
def step_impl(context, a, b):
    context.calculator.multiply(a, b)

@then('the result should be {expected:d}')
def step_impl(context, expected):
    assert context.calculator.result == expected, \
        f"Expected {expected}, but got {context.calculator.result}"
