// Thin wrapper around the backend REST API.

export class ApiError extends Error {
	constructor(status, detail) {
		super(detail || `Request failed with status ${status}`);
		this.status = status;
	}
}

async function request(method, url, body) {
	const options = { method, headers: { Accept: 'application/json' } };
	if (body !== undefined) {
		options.headers['Content-Type'] = 'application/json';
		options.body = JSON.stringify(body);
	}
	const response = await fetch(url, options);
	const text = await response.text();
	const data = text ? JSON.parse(text) : null;
	if (!response.ok) {
		throw new ApiError(response.status, data?.detail);
	}
	return data;
}

const RULES = '/api/assignment-rules';

export const listRules = () => request('GET', RULES);
export const createRule = (rule) => request('POST', RULES, rule);
export const updateRule = (id, rule) => request('PUT', `${RULES}/${encodeURIComponent(id)}`, rule);
export const setRuleEnabled = (id, enabled) =>
	request('PATCH', `${RULES}/${encodeURIComponent(id)}/enabled`, { enabled });
export const deleteRule = (id) => request('DELETE', `${RULES}/${encodeURIComponent(id)}`);
export const getConditionTypes = () => request('GET', `${RULES}/condition-types`);
export const listMachines = () => request('GET', '/api/machines');
