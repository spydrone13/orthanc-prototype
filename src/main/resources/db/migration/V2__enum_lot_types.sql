-- Chip type and process type became enums and the STARTS_WITH / MATCHES operators were removed.
-- Rules that can no longer be represented are deleted as a whole: dropping only the offending
-- condition would silently widen what the rule matches. Conditions and operands cascade.
delete from assignment_rule
where id in (
    select c.rule_id
    from assignment_rule_condition c
    where c.operator in ('STARTS_WITH', 'MATCHES')
       or (c.attribute = 'CHIP_TYPE' and exists (
               select 1 from assignment_rule_condition_operand o
               where o.condition_id = c.id
                 and o.operand_value not in ('X_SERIES', 'Z_SERIES', 'Q_SERIES')))
       or (c.attribute = 'PROCESS_TYPE' and exists (
               select 1 from assignment_rule_condition_operand o
               where o.condition_id = c.id
                 and o.operand_value not in ('LEGACY', 'STANDARD', 'NEXTGEN')))
);
