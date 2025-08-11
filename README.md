# API Test Framework (Java 18, TestNG, Rest Assured)

## Overview
API testing framework built for comprehensive Player Controller testing:

- **Java 18** - Modern Java features and performance
- **TestNG 7.9.0** - Testing framework with parallel execution support
- **Rest Assured 5.4.0** - REST API testing with JSON schema validation
- **Allure 2.29.0** - Comprehensive test reporting with Rest Assured integration
- **Log4j2 2.23.1** - Structured logging via SLF4J facade
- **Owner 1.0.12** - Type-safe configuration management with environment support
- **DataFaker 2.3.1** - Realistic test data generation
- **Jackson 2.17.2** - JSON serialization/deserialization
- **Apache Commons Configuration 2.10.1** - Robust property management

## Test Task Coverage

This framework comprehensively covers the Player Controller API requirements:

### Business Rules Validated
- ✅ **Age Validation**: Users must be 16-60 years old
- ✅ **Role-Based Access Control**: Only supervisor/admin can create users
- ✅ **Role Restrictions**: Users can only be created with 'admin' or 'user' roles
- ✅ **Unique Constraints**: Login and screenName must be unique
- ✅ **Password Requirements**: Latin letters + numbers, 7-15 characters
- ✅ **Gender Validation**: Only 'male' or 'female' allowed
- ✅ **Required Fields**: All mandatory fields validated

### Role-Based Permissions Tested
- **Supervisor**: Full system access (except deleting other supervisors)
- **Admin**: Can manage users and admins (except self-deletion)
- **User**: Can only manage their own account (except deletion)

## Configuration Management

### Environment-Based Configuration
The framework uses **Owner** library for type-safe, environment-aware configuration:

- **Environment Detection**: Automatically loads configuration based on `env` system property
- **Fallback Strategy**: Falls back to `prod` environment if not specified

### Configuration Files
- `src/main/resources/prod/config.properties` - Production environment configuration
- `src/main/resources/log4j2.xml` - Main logging configuration
- `src/test/resources/log4j2-test.xml` - Test-specific logging configuration

## Running Tests

### Basic Execution
```bash
# Run with default configuration (3 threads, prod environment)
mvn test

# Run with custom thread count
mvn test -Dparallel=methods -DthreadCount=3 

# Run with custom environment
mvn test -Denv=staging

# Override base URL
mvn test -D base.url=https://your-api-host 
```

### Running Specific Test Classes
```bash
# Run only create tests
mvn test -Dtest=PlayerCreateTest

# Run integration tests only
mvn test -Dtest=PlayerControllerTest

```

### Parallel Execution
The framework supports parallel test execution through:
- **TestNG Suite**: Configured in `testng.xml` with parameterized thread count
- **Maven Surefire**: Passes thread count and parallel mode as system properties
- **AspectJ Weaver**: Enables Allure step annotations in parallel execution

### Allure Reporting
```bash
# Generate HTML report
mvn allure:report

# Serve report locally
mvn allure:serve

# View results in target/allure-report/
```

## Found Issues

### 🚨 Critical REST API Implementation Issues

#### 1. **Incorrect HTTP Status Codes**
- **GET Non-existent Player**: Returns `200` with error body instead of `404`
  - **Impact**: Violates REST principles, confuses clients
  - **Expected**: `404 Not Found` for non-existent resources
  - **Test Coverage**: ✅ Documented and tested

- **DELETE Non-existent Player**: Returns `403` instead of `404`
  - **Impact**: Authorization error instead of resource not found
  - **Expected**: `404 Not Found` for non-existent resources
  - **Test Coverage**: ✅ Documented and tested

#### 2. **Inconsistent Error Response Patterns**
- **Mixed Status Codes**: Same error types return different status codes
- **Error Body Inconsistency**: Some errors return error objects, others don't
- **Missing Error Details**: Some endpoints lack proper error messages

#### 3. **Authorization Logic Issues**
- **Self-deletion Prevention**: Users cannot delete themselves (business rule)
- **Role Hierarchy**: Supervisor deletion restrictions not clearly implemented
- **Cross-role Operations**: Some role-based restrictions may be inconsistent

### 🟡 Business Logic Concerns

#### 4. **Data Validation Edge Cases**
- **Age Boundaries**: Exact boundary values (16, 60) may have edge case issues
- **Password Complexity**: Special characters not clearly defined
- **Unique Constraint Handling**: Race conditions in concurrent operations

#### 5. **API Design Inconsistencies**
- **GET used instead of POST to CREATE user
  - **Impact**: Violates REST principles, data vulnerability, confuses clients
- **POST used instead of GET to GET user
  - **Impact**: Violates REST principles, confuses clients
- **Endpoint Patterns**: Mixed use of path parameters vs request body
- **Response Formats**: Inconsistent response structures across endpoints
- **Error Message Localization**: No internationalization support
