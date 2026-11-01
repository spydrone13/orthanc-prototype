// Page controller for rules.html: lists assignment rules and edits them in a modal.
// All data is rendered via textContent/createElement so user input can't inject markup.

import * as api from './api.js';

const ATTRIBUTE_LABELS = {
	CHIP_TYPE: 'Chip type',
	PROCESS_TYPE: 'Process type',
	WAFER_COUNT: 'Wafer count',
};

const OPERATOR_LABELS = {
	EQUALS: '=',
	IN: 'in',
	NOT_IN: 'not in',
	LESS_THAN: '<',
	LESS_THAN_OR_EQUAL: '≤',
	GREATER_THAN: '>',
	GREATER_THAN_OR_EQUAL: '≥',
	BETWEEN: 'between',
};

const state = {
	rules: [],
	machines: [],
	attributes: [],
	operators: [],
	editingRuleId: null,
	deletingRule: null,
};

const el = (id) => document.getElementById(id);

const ruleModal = bootstrap.Modal.getOrCreateInstance(el('rule-modal'));
const deleteModal = bootstrap.Modal.getOrCreateInstance(el('delete-modal'));
const toast = bootstrap.Toast.getOrCreateInstance(el('toast'));

// ---------------------------------------------------------------------------
// Helpers

function createElement(tag, { className, text, attrs } = {}, children = []) {
	const node = document.createElement(tag);
	if (className) node.className = className;
	if (text !== undefined) node.textContent = text;
	for (const [name, value] of Object.entries(attrs ?? {})) node.setAttribute(name, value);
	node.append(...children);
	return node;
}

const attributeLabel = (name) => ATTRIBUTE_LABELS[name] ?? name;
const operatorLabel = (name) => OPERATOR_LABELS[name] ?? name;
const attributeType = (name) => state.attributes.find((a) => a.name === name);
const attributeKind = (name) => attributeType(name)?.kind;
const operatorType = (name) => state.operators.find((o) => o.name === name);
const operatorsFor = (attribute) => state.operators.filter((o) => o.kinds.includes(attributeKind(attribute)));
const isListOperator = (operator) => operatorType(operator)?.maxOperands === null;

function describeCondition({ attribute, operator, values }) {
	const label = attributeLabel(attribute);
	if (operator === 'BETWEEN') return `${label} between ${values[0]} and ${values[1]}`;
	if (isListOperator(operator)) return `${label} ${operatorLabel(operator)} [${values.join(', ')}]`;
	return `${label} ${operatorLabel(operator)} ${values.join(', ')}`;
}

function machineLabel(machineId) {
	const machine = state.machines.find((m) => m.id === machineId);
	return machine ? `${machine.id} – ${machine.name}` : `${machineId} (unknown)`;
}

function showToast(message) {
	el('toast-body').textContent = message;
	toast.show();
}

function showAlert(alertId, message) {
	const alert = el(alertId);
	alert.textContent = message;
	alert.classList.toggle('d-none', !message);
}

// ---------------------------------------------------------------------------
// Rules table

function renderRules() {
	const body = el('rules-body');
	body.replaceChildren();
	if (state.rules.length === 0) {
		body.append(createElement('tr', {}, [
			createElement('td', {
				className: 'text-center text-body-secondary py-4',
				text: 'No rules yet. Lots will stay unassigned until you create one.',
				attrs: { colspan: '6' },
			}),
		]));
		return;
	}
	for (const rule of state.rules) {
		body.append(renderRuleRow(rule));
	}
}

function renderRuleRow(rule) {
	const badges = rule.conditions.length === 0
		? [createElement('span', { className: 'text-body-secondary fst-italic', text: 'matches every lot' })]
		: rule.conditions.map((condition) =>
			createElement('span', { className: 'badge text-bg-light border condition-badge', text: describeCondition(condition) }));

	const toggle = createElement('input', {
		className: 'form-check-input',
		attrs: { type: 'checkbox', role: 'switch', 'aria-label': `Enable rule ${rule.name}` },
	});
	toggle.checked = rule.enabled;
	toggle.addEventListener('change', () => toggleEnabled(rule, toggle));

	const editButton = createElement('button', { className: 'btn btn-sm btn-outline-secondary', text: 'Edit', attrs: { type: 'button' } });
	editButton.addEventListener('click', () => openEditor(rule));
	const deleteButton = createElement('button', { className: 'btn btn-sm btn-outline-danger', text: 'Delete', attrs: { type: 'button' } });
	deleteButton.addEventListener('click', () => openDeleteConfirmation(rule));

	return createElement('tr', { className: rule.enabled ? '' : 'text-body-secondary' }, [
		createElement('td', { className: 'text-end fw-semibold', text: String(rule.priority) }),
		createElement('td', { text: rule.name }),
		createElement('td', {}, [createElement('div', { className: 'condition-badges' }, badges)]),
		createElement('td', { text: machineLabel(rule.targetMachineId) }),
		createElement('td', {}, [createElement('div', { className: 'form-check form-switch mb-0' }, [toggle])]),
		createElement('td', { className: 'text-end text-nowrap' }, [
			createElement('div', { className: 'btn-group' }, [editButton, deleteButton]),
		]),
	]);
}

async function reloadRules() {
	state.rules = await api.listRules();
	renderRules();
}

async function toggleEnabled(rule, toggle) {
	toggle.disabled = true;
	try {
		await api.setRuleEnabled(rule.id, toggle.checked);
		showAlert('page-alert', '');
		showToast(`Rule "${rule.name}" ${toggle.checked ? 'enabled' : 'disabled'}.`);
		await reloadRules();
	} catch (error) {
		toggle.checked = !toggle.checked;
		toggle.disabled = false;
		showAlert('page-alert', error.message);
	}
}

// ---------------------------------------------------------------------------
// Rule editor: condition rows

function fillSelect(select, options, selected) {
	select.replaceChildren(...options.map(({ value, label }) => {
		const option = createElement('option', { text: label, attrs: { value } });
		option.selected = value === selected;
		return option;
	}));
}

function addConditionRow(condition) {
	const row = el('condition-row-template').content.firstElementChild.cloneNode(true);
	const attributeSelect = row.querySelector('.condition-attribute');
	const operatorSelect = row.querySelector('.condition-operator');

	fillSelect(attributeSelect,
		state.attributes.map((a) => ({ value: a.name, label: attributeLabel(a.name) })),
		condition?.attribute ?? state.attributes[0]?.name);

	const refreshOperators = (selectedOperator) => {
		const operators = operatorsFor(attributeSelect.value);
		const selected = operators.some((o) => o.name === selectedOperator) ? selectedOperator : operators[0]?.name;
		fillSelect(operatorSelect, operators.map((o) => ({ value: o.name, label: operatorLabel(o.name) })), selected);
	};

	attributeSelect.addEventListener('change', () => {
		const values = readValues(row);
		refreshOperators(operatorSelect.value);
		renderValueInputs(row, values);
	});
	operatorSelect.addEventListener('change', () => renderValueInputs(row, readValues(row)));
	row.querySelector('.remove-condition-button').addEventListener('click', () => {
		row.remove();
		updateNoConditionsHint();
	});

	refreshOperators(condition?.operator);
	renderValueInputs(row, condition?.values ?? []);
	el('conditions-list').append(row);
	updateNoConditionsHint();
}

function numberInput(value, placeholder) {
	const input = createElement('input', {
		className: 'form-control condition-value',
		attrs: { type: 'number', step: '1', required: '', placeholder },
	});
	input.value = value ?? '';
	return input;
}

let checkboxIdCounter = 0;

// Enumerated attributes: a dropdown for single-value operators, a checkbox group for IN / NOT_IN.
function renderEnumInputs(container, attribute, type, values) {
	const allowed = attributeType(attribute).values;

	if (type.maxOperands === null) {
		const checks = allowed.map((value) => {
			const id = `condition-check-${++checkboxIdCounter}`;
			const checkbox = createElement('input', { className: 'form-check-input', attrs: { type: 'checkbox', id, value } });
			checkbox.checked = values.includes(value);
			checkbox.addEventListener('change', () => {
				if (el('rule-form').classList.contains('was-validated')) validateCheckboxGroups();
			});
			return createElement('div', { className: 'form-check form-check-inline' }, [
				checkbox,
				createElement('label', { className: 'form-check-label', text: value, attrs: { for: id } }),
			]);
		});
		container.replaceChildren(
			createElement('div', { className: 'condition-value-checks pt-2' }, checks),
			createElement('div', { className: 'invalid-feedback', text: 'Select at least one value.' }));
		return;
	}

	const select = createElement('select', { className: 'form-select condition-value', attrs: { 'aria-label': 'Value', required: '' } });
	fillSelect(select, allowed.map((value) => ({ value, label: value })), allowed.includes(values[0]) ? values[0] : allowed[0]);
	container.replaceChildren(select);
}

// Renders the value inputs for the row's current attribute/operator, keeping previously entered values.
function renderValueInputs(row, values) {
	const container = row.querySelector('.condition-values');
	const attribute = row.querySelector('.condition-attribute').value;
	const operator = row.querySelector('.condition-operator').value;
	const type = operatorType(operator);

	if (attributeKind(attribute) === 'ENUMERATED') {
		renderEnumInputs(container, attribute, type, values);
		return;
	}

	if (type.maxOperands === null) {
		const input = createElement('input', {
			className: 'form-control condition-value-list',
			attrs: {
				type: 'text',
				required: '',
				placeholder: 'Comma-separated, e.g. 20, 25',
				pattern: '\\s*-?\\d+(\\s*,\\s*-?\\d+)*\\s*',
			},
		});
		input.value = values.join(', ');
		container.replaceChildren(input,
			createElement('div', { className: 'invalid-feedback', text: 'Enter whole numbers separated by commas.' }));
		return;
	}

	if (type.minOperands === 2) {
		container.replaceChildren(createElement('div', { className: 'input-group has-validation' }, [
			numberInput(values[0], 'min'),
			createElement('span', { className: 'input-group-text', text: 'and' }),
			numberInput(values[1], 'max'),
			createElement('div', { className: 'invalid-feedback', text: 'Enter both bounds.' }),
		]));
		return;
	}

	container.replaceChildren(numberInput(values[0], 'Value'),
		createElement('div', { className: 'invalid-feedback', text: 'Enter a value.' }));
}

// Checkbox groups have no native "required", so they're validated by hand. Returns true if all are valid.
function validateCheckboxGroups() {
	let valid = true;
	for (const group of el('conditions-list').querySelectorAll('.condition-value-checks')) {
		const groupValid = group.querySelector('input:checked') !== null;
		group.classList.toggle('is-invalid', !groupValid);
		valid &&= groupValid;
	}
	return valid;
}

function readValues(row) {
	const checks = row.querySelector('.condition-value-checks');
	if (checks) {
		return [...checks.querySelectorAll('input:checked')].map((checkbox) => checkbox.value);
	}
	const list = row.querySelector('.condition-value-list');
	if (list) {
		return list.value.split(',').map((v) => v.trim()).filter((v) => v !== '');
	}
	return [...row.querySelectorAll('.condition-value')].map((input) => input.value.trim());
}

function readConditions() {
	return [...el('conditions-list').querySelectorAll('.condition-row')].map((row) => ({
		attribute: row.querySelector('.condition-attribute').value,
		operator: row.querySelector('.condition-operator').value,
		values: readValues(row),
	}));
}

function updateNoConditionsHint() {
	const empty = el('conditions-list').children.length === 0;
	el('no-conditions-hint').classList.toggle('d-none', !empty);
}

// ---------------------------------------------------------------------------
// Rule editor: modal

function openEditor(rule) {
	state.editingRuleId = rule?.id ?? null;
	const form = el('rule-form');
	form.classList.remove('was-validated');
	showAlert('rule-form-alert', '');

	el('rule-modal-title').textContent = rule ? `Edit rule "${rule.name}"` : 'New rule';
	el('rule-name').value = rule?.name ?? '';
	el('rule-priority').value = rule?.priority ?? nextFreePriority();

	const machineOptions = state.machines.map((m) => ({ value: m.id, label: `${m.id} – ${m.name}` }));
	if (rule && !state.machines.some((m) => m.id === rule.targetMachineId)) {
		machineOptions.push({ value: rule.targetMachineId, label: machineLabel(rule.targetMachineId) });
	}
	fillSelect(el('rule-machine'), [{ value: '', label: 'Select a machine…' }, ...machineOptions], rule?.targetMachineId ?? '');

	el('conditions-list').replaceChildren();
	(rule?.conditions ?? []).forEach(addConditionRow);
	updateNoConditionsHint();

	ruleModal.show();
}

function nextFreePriority() {
	const highest = Math.max(0, ...state.rules.map((r) => r.priority));
	return state.rules.length === 0 ? 10 : (Math.floor(highest / 10) + 1) * 10;
}

async function saveRule(event) {
	event.preventDefault();
	const form = el('rule-form');
	form.classList.add('was-validated');
	const checkboxesValid = validateCheckboxGroups();
	if (!form.checkValidity() || !checkboxesValid) {
		return;
	}

	const payload = {
		name: el('rule-name').value.trim(),
		priority: Number(el('rule-priority').value),
		targetMachineId: el('rule-machine').value,
		conditions: readConditions(),
	};

	const saveButton = el('save-rule-button');
	saveButton.disabled = true;
	try {
		if (state.editingRuleId) {
			await api.updateRule(state.editingRuleId, payload);
		} else {
			await api.createRule(payload);
		}
		ruleModal.hide();
		showToast(`Rule "${payload.name}" saved.`);
		await reloadRules();
	} catch (error) {
		showAlert('rule-form-alert', error.message);
	} finally {
		saveButton.disabled = false;
	}
}

// ---------------------------------------------------------------------------
// Delete

function openDeleteConfirmation(rule) {
	state.deletingRule = rule;
	el('delete-rule-name').textContent = rule.name;
	deleteModal.show();
}

async function confirmDelete() {
	const rule = state.deletingRule;
	const button = el('confirm-delete-button');
	button.disabled = true;
	try {
		await api.deleteRule(rule.id);
		showAlert('page-alert', '');
		showToast(`Rule "${rule.name}" deleted.`);
		await reloadRules();
	} catch (error) {
		showAlert('page-alert', error.message);
	} finally {
		button.disabled = false;
		deleteModal.hide();
	}
}

// ---------------------------------------------------------------------------
// Startup

async function init() {
	el('new-rule-button').addEventListener('click', () => openEditor(null));
	el('add-condition-button').addEventListener('click', () => addConditionRow(null));
	el('rule-form').addEventListener('submit', saveRule);
	el('confirm-delete-button').addEventListener('click', confirmDelete);

	try {
		const [machines, conditionTypes, rules] = await Promise.all([
			api.listMachines(), api.getConditionTypes(), api.listRules(),
		]);
		state.machines = machines;
		state.attributes = conditionTypes.attributes;
		state.operators = conditionTypes.operators;
		state.rules = rules;
		renderRules();
	} catch (error) {
		showAlert('page-alert', `Could not load rules: ${error.message}`);
	}
}

init();
