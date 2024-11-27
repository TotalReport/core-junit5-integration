### Default cases to statuses

#### `@BeforeAll`, `@BeforeEach`, `@AfterAll`, `@AfterEach`
##### Timeout without interruption
```java
@Timeout(value = 1, unit = TimeUnit.NANOSECONDS)
```
The status is AUTOMATION_BUG.

##### Timeout with interruption
```java
@Timeout(value = 1, unit = TimeUnit.NANOSECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
```
The status is AUTOMATION_BUG, because there is no way to distinct aborted from not aborted in code for `@BeforeAll`, `@BeforeEach`, `@AfterAll`, `@AfterEach`.

#### `@Test`
##### Timeout without interruption
```java
@Timeout(value = 1, unit = TimeUnit.NANOSECONDS)
```
The status is AUTOMATION_BUG.

##### Timeout with interruption
```java
@Timeout(value = 1, unit = TimeUnit.NANOSECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
```
The status is ABORTED.