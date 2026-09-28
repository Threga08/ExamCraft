let currentPaper = null;
const $ = (selector) => document.querySelector(selector);
const message = (text, error = true) => { $('#message').textContent = text; $('#message').style.color = error ? '#9b3725' : '#24624e'; };
async function api(url, options = {}) {
  const response = await fetch(url, { headers: { 'Content-Type': 'application/json' }, ...options });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(data.message || 'Request failed.');
  return data;
}
function formData(form) { return Object.fromEntries(new FormData(form)); }
function addTextCell(row, value) {
  const cell = document.createElement('td');
  cell.textContent = value ?? '';
  row.append(cell);
}
async function loadUnits() {
  const units = await api('/api/units');
  const select = $('#unit-select');
  select.replaceChildren(new Option('Select unit', ''));
  units.forEach(unit => select.add(new Option(unit.unitName, unit.id)));
}
async function loadBank() {
  const questions = await api('/api/questions');
  const table = document.createElement('table');
  const head = document.createElement('thead');
  const headingRow = document.createElement('tr');
  ['ID', 'Question', 'Topic', 'Unit', 'Difficulty', 'Usage'].forEach(label => {
    const heading = document.createElement('th'); heading.textContent = label; headingRow.append(heading);
  });
  head.append(headingRow);
  const body = document.createElement('tbody');
  questions.forEach(question => {
    const row = document.createElement('tr');
    addTextCell(row, question.id); addTextCell(row, question.questionText); addTextCell(row, question.topic);
    addTextCell(row, question.unit?.unitName || '-'); addTextCell(row, question.difficulty);
    addTextCell(row, question.usageCount || 0); body.append(row);
  });
  table.append(head, body);
  $('#bank').replaceChildren(table);
}
$('#unit-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    await api('/api/units', { method: 'POST', body: JSON.stringify(formData(event.target)) });
    event.target.reset(); await loadUnits(); message('Unit added.', false);
  } catch (error) { message(error.message); }
});
function renderPaper(paper) {
  const list = $('#generated'); list.replaceChildren();
  paper.questions.forEach((question, index) => {
    const card = document.createElement('div'); card.className = 'question-card';
    const prompt = document.createElement('p');
    const title = document.createElement('strong'); title.textContent = `${index + 1}. ${question.questionText}`;
    prompt.append(title); card.append(prompt);
    ['A', 'B', 'C', 'D'].forEach(letter => {
      const label = document.createElement('label');
      const input = document.createElement('input');
      input.type = 'radio'; input.name = `answer-${question.id}`; input.value = letter;
      label.append(input, ` ${letter}. ${question[`option${letter}`]}`);
      card.append(label, document.createElement('br'));
    });
    list.append(card);
  });
}
$('#question-form').addEventListener('submit', async (event) => {
  event.preventDefault(); const data = formData(event.target);
  data.unit = { id: Number(data.unitId) }; delete data.unitId;
  try { await api('/api/questions', { method: 'POST', body: JSON.stringify(data) }); event.target.reset(); message('Question added.', false); await loadBank(); }
  catch (error) { message(error.message); }
});
$('#generate-form').addEventListener('submit', async (event) => {
  event.preventDefault(); const data = formData(event.target); Object.keys(data).forEach(k => data[k] = Number(data[k]));
  try { currentPaper = await api('/api/test-papers/generate', { method: 'POST', body: JSON.stringify(data) });
    renderPaper(currentPaper); $('#result').replaceChildren(); message('Test generated.', false); await loadBank();
  } catch (error) { message(error.message); }
});
$('#attempt-form').addEventListener('submit', async (event) => {
  event.preventDefault(); if (!currentPaper) return message('Generate a test before submitting an attempt.');
  const answers = {}; currentPaper.questions.forEach(q => { const selected = document.querySelector(`input[name="answer-${q.id}"]:checked`); if (selected) answers[q.id] = selected.value; });
  try {
    const result = await api('/api/attempts', { method: 'POST', body: JSON.stringify({ studentName: formData(event.target).studentName, testPaperId: currentPaper.id, answers }) });
    const summary = document.createElement('p');
    summary.textContent = `Total: ${result.totalQuestions} | Correct: ${result.correctAnswers} | Wrong: ${result.wrongAnswers} | Score: ${result.score}`;
    $('#result').replaceChildren(summary); message('Attempt graded.', false);
  } catch (error) { message(error.message); }
});
Promise.all([loadUnits(), loadBank()]).catch(error => message(error.message));
