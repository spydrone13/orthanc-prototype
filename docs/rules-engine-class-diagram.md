# Rules Engine – Class Diagrams

The lot assignment rules engine follows a hexagonal (ports and adapters) layout:

- **`domain`**: plain Java, with no framework code.
- **`application`**: use cases (inbound ports), repository interfaces (outbound ports) and the services that implement the use cases.
- **`adapter`**: the REST, persistence and configuration code that plugs into those ports.

Source root: `src/main/java/com/orthanc/orthanc_prototype/`.

## 1. Domain model

An `AssignmentRule` routes a `Lot` to a target machine when **all** of its conditions match. `AssignmentRuleEvaluator` picks the enabled rule with the lowest priority that matches.

```mermaid
classDiagram
    direction LR

    namespace lot_domain {
        class Lot {
            -ChipType chipType
            -ProcessType processType
            -int waferCount
            +Lot(ChipType, ProcessType, int)
            +getChipType() ChipType
            +getProcessType() ProcessType
            +getWaferCount() int
            +setWaferCount(int)
        }
        class ChipType {
            <<enumeration>>
            X_SERIES
            Z_SERIES
            Q_SERIES
        }
        class ProcessType {
            <<enumeration>>
            LEGACY
            STANDARD
            NEXTGEN
        }
    }

    namespace assignment_domain {
        class AssignmentRule {
            <<AggregateRoot>>
            -AssignmentRuleId id
            -String name
            -int priority
            -boolean enabled
            -List~Condition~ conditions
            -String targetMachineId
            +create(name, priority, conditions, targetMachineId)$ AssignmentRule
            +rehydrate(id, name, priority, enabled, conditions, targetMachineId)$ AssignmentRule
            +update(name, priority, conditions, targetMachineId)
            +enable()
            +disable()
            +matches(Lot) boolean
        }
        class AssignmentRuleId {
            <<ValueObject>>
            +UUID value
            +newId()$ AssignmentRuleId
            +of(String)$ AssignmentRuleId
        }
        class Condition {
            <<sealed interface>>
            +attribute() LotAttribute
            +operator() Operator
            +operands() List~String~
            +matches(Lot) boolean
            +of(LotAttribute, Operator, List~String~)$ Condition
        }
        class EnumCondition {
            <<ValueObject>>
            -LotAttribute attribute
            -Operator operator
            -List~String~ operands
            +matches(Lot) boolean
        }
        class WaferCountCondition {
            <<ValueObject>>
            -Operator operator
            -List~Integer~ values
            +matches(Lot) boolean
        }
        class LotAttribute {
            <<enumeration>>
            CHIP_TYPE
            PROCESS_TYPE
            WAFER_COUNT
            +kind() Kind
            +allowedValues() List~String~
        }
        class Kind {
            <<enumeration>>
            ENUMERATED
            NUMERIC
        }
        class Operator {
            <<enumeration>>
            EQUALS
            IN
            NOT_IN
            LESS_THAN
            LESS_THAN_OR_EQUAL
            GREATER_THAN
            GREATER_THAN_OR_EQUAL
            BETWEEN
            +supports(Kind) boolean
            +supportedKinds() Set~Kind~
            +minOperands() int
            +maxOperands() int
        }
        class AssignmentRuleEvaluator {
            <<DomainService>>
            +selectRuleFor(Lot, Collection~AssignmentRule~) Optional~AssignmentRule~
        }
        class InvalidAssignmentRuleException {
            <<exception>>
        }
        class DuplicateRulePriorityException {
            <<exception>>
        }
        class AssignmentRuleNotFoundException {
            <<exception>>
        }
    }

    Lot --> ChipType
    Lot --> ProcessType

    AssignmentRule *-- AssignmentRuleId : identity
    AssignmentRule *-- "0..*" Condition : all must match
    Condition <|.. EnumCondition
    Condition <|.. WaferCountCondition
    Condition --> LotAttribute
    Condition --> Operator
    LotAttribute --> Kind
    Operator ..> Kind : supports
    LotAttribute ..> ChipType : allowed values
    LotAttribute ..> ProcessType : allowed values

    AssignmentRuleEvaluator ..> AssignmentRule : selects
    AssignmentRule ..> Lot : evaluates
    Condition ..> Lot : evaluates
    Condition ..> InvalidAssignmentRuleException : throws
```

### Operator support

| Operator | Chip type / process type (`ENUMERATED`) | Wafer count (`NUMERIC`) | Operands |
|---|:-:|:-:|---|
| `EQUALS` | ✓ | ✓ | 1 |
| `IN` / `NOT_IN` | ✓ | ✓ | 1 or more |
| `LESS_THAN`, `LESS_THAN_OR_EQUAL`, `GREATER_THAN`, `GREATER_THAN_OR_EQUAL` | | ✓ | 1 |
| `BETWEEN` (inclusive) | | ✓ | 2 (min ≤ max) |

## 2. Hexagonal architecture: ports and adapters

```mermaid
classDiagram
    direction TB

    namespace inbound_adapters_web {
        class AssignmentRuleController {
            <<RestController>>
            POST /api/assignment-rules
            GET /api/assignment-rules
            GET /api/assignment-rules/id
            PUT /api/assignment-rules/id
            PATCH /api/assignment-rules/id/enabled
            DELETE /api/assignment-rules/id
        }
        class ConditionMetadataController {
            <<RestController>>
            GET /api/assignment-rules/condition-types
        }
        class LotAssignmentController {
            <<RestController>>
            POST /api/lots/auto-assignments
        }
        class AssignmentExceptionHandler {
            <<RestControllerAdvice>>
            maps domain exceptions to ProblemDetail
        }
    }

    namespace inbound_ports {
        class ManageAssignmentRulesUseCase {
            <<interface>>
            +create(SaveAssignmentRuleCommand) AssignmentRule
            +update(AssignmentRuleId, SaveAssignmentRuleCommand) AssignmentRule
            +setEnabled(AssignmentRuleId, boolean) AssignmentRule
            +delete(AssignmentRuleId)
            +get(AssignmentRuleId) AssignmentRule
            +listAll() List~AssignmentRule~
        }
        class AutoAssignLotUseCase {
            <<interface>>
            +autoAssign(AutoAssignLotCommand) AssignmentOutcome
        }
        class SaveAssignmentRuleCommand {
            <<record>>
            name
            priority
            List~ConditionSpec~ conditions
            targetMachineId
        }
        class AutoAssignLotCommand {
            <<record>>
            ChipType chipType
            ProcessType processType
            int waferCount
        }
        class AssignmentOutcome {
            <<sealed interface>>
            Assigned(machineId, ruleId)
            Unassigned()
        }
    }

    namespace application_services {
        class AssignmentRuleService {
            <<Service>>
            validates target machine exists
            enforces unique priority
        }
        class LotAutoAssignmentService {
            <<Service>>
            evaluates rules, assigns lot to machine
        }
    }

    namespace outbound_ports {
        class AssignmentRuleRepository {
            <<interface>>
            +save(AssignmentRule) AssignmentRule
            +findById(AssignmentRuleId) Optional~AssignmentRule~
            +findAll() List~AssignmentRule~
            +deleteById(AssignmentRuleId)
            +existsOtherWithPriority(int, AssignmentRuleId) boolean
        }
        class MachineRepository {
            <<interface>>
            +findAll() List~EtchingMachine~
            +findById(String) Optional~EtchingMachine~
            +existsById(String) boolean
            +save(EtchingMachine)
        }
    }

    namespace outbound_adapters {
        class AssignmentRulePersistenceAdapter {
            <<Repository>>
            JPA + H2, Flyway schema
        }
        class AssignmentRuleMapper {
            domain to JPA entity mapping
        }
        class AssignmentRuleJpaEntity {
            <<Entity>>
            table assignment_rule
        }
        class ConditionJpaEntity {
            <<Entity>>
            table assignment_rule_condition
        }
        class SpringDataAssignmentRuleRepository {
            <<JpaRepository>>
        }
        class InMemoryMachineRepository {
            <<Repository>>
            seeded from orthanc.machines config
        }
    }

    namespace domain {
        class AssignmentRule {
            <<AggregateRoot>>
        }
        class AssignmentRuleEvaluator {
            <<DomainService>>
        }
        class EtchingMachine {
            <<AggregateRoot>>
            +assignLot(Lot)
        }
    }

    AssignmentRuleController --> ManageAssignmentRulesUseCase
    LotAssignmentController --> AutoAssignLotUseCase
    ManageAssignmentRulesUseCase ..> SaveAssignmentRuleCommand
    AutoAssignLotUseCase ..> AutoAssignLotCommand
    AutoAssignLotUseCase ..> AssignmentOutcome

    ManageAssignmentRulesUseCase <|.. AssignmentRuleService
    AutoAssignLotUseCase <|.. LotAutoAssignmentService

    AssignmentRuleService --> AssignmentRuleRepository
    AssignmentRuleService --> MachineRepository
    LotAutoAssignmentService --> AssignmentRuleRepository
    LotAutoAssignmentService --> MachineRepository
    LotAutoAssignmentService --> AssignmentRuleEvaluator

    AssignmentRuleService ..> AssignmentRule
    AssignmentRuleEvaluator ..> AssignmentRule
    LotAutoAssignmentService ..> EtchingMachine : assignLot

    AssignmentRuleRepository <|.. AssignmentRulePersistenceAdapter
    MachineRepository <|.. InMemoryMachineRepository
    AssignmentRulePersistenceAdapter --> SpringDataAssignmentRuleRepository
    AssignmentRulePersistenceAdapter ..> AssignmentRuleMapper
    AssignmentRuleMapper ..> AssignmentRuleJpaEntity
    AssignmentRuleJpaEntity *-- "0..*" ConditionJpaEntity : ordered
```

### Dependency rules

`HexagonalArchitectureTest` enforces these with ArchUnit:

- `..domain..` must not depend on Spring, Jakarta, Hibernate, Jackson, `..application..` or `..adapter..`.
- `..application..` must not depend on `..adapter..`.

## 3. Auto-assignment flow

```mermaid
sequenceDiagram
    participant Client
    participant C as LotAssignmentController
    participant S as LotAutoAssignmentService
    participant R as AssignmentRuleRepository
    participant E as AssignmentRuleEvaluator
    participant M as MachineRepository

    Client->>C: POST /api/lots/auto-assignments
    C->>S: autoAssign(AutoAssignLotCommand)
    S->>S: new Lot(chipType, processType, waferCount)
    S->>R: findAll()
    R-->>S: rules ordered by priority
    S->>E: selectRuleFor(lot, rules)
    alt an enabled rule matches
        E-->>S: rule
        S->>M: findById(rule.targetMachineId)
        S->>S: machine.assignLot(lot)
        S->>M: save(machine)
        S-->>C: Assigned(machineId, ruleId)
    else no rule matches
        E-->>S: empty
        S-->>C: Unassigned
    end
    C-->>Client: status, machineId, ruleId
```
