'use strict';
const $ = (id) => document.getElementById(id);
let page = 0;
let query = '';
let currentBooks = [];
let totalPages = 0;
let requestSequence = 0;

async function api(url, options = {}) {
  const response = await fetch(url, { ...options, headers: { 'Content-Type': 'application/json', ...options.headers } });
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error(error.errors?.map((item) => `${item.field}: ${item.message}`).join('; ') || error.detail || `Request failed (${response.status})`);
  }
  return response.status === 204 ? null : response.json();
}

function notice(message, error = false) {
  $('notice').textContent = message;
  $('notice').className = error ? 'error' : '';
}

function element(tag, className, text) {
  const node = document.createElement(tag);
  node.className = className;
  if (text !== undefined) node.textContent = text;
  return node;
}

function renderCard(book) {
  const card = element('article', 'card');
  const top = element('div', 'card-top');
  top.append(element('div', 'monogram', book.title.charAt(0).toUpperCase()), element('span', `badge${book.borrowed ? ' loan' : ''}`, book.borrowed ? '↗ Borrowed' : '● Available'));
  card.append(top, element('h3', '', book.title), element('p', 'author', book.author), element('p', 'isbn', `ISBN ${book.isbn}`));
  const actions = element('div', 'actions');
  const loan = element('button', 'loan-action', book.borrowed ? 'Return ↙' : 'Borrow ↗');
  loan.onclick = async () => {
    loan.disabled = true;
    try {
      const link = book._links[book.borrowed ? 'return' : 'borrow'];
      await api(link.href, { method: 'POST' });
      await load();
      notice(book.borrowed ? 'Book returned to the shelf.' : 'Book borrowed. Enjoy your next chapter.');
    } catch (error) { notice(error.message, true); }
    finally { loan.disabled = false; }
  };
  const edit = element('button', '', 'Edit');
  edit.onclick = () => openEditor(book);
  const remove = element('button', '', 'Delete');
  remove.disabled = book.borrowed;
  remove.title = book.borrowed ? 'Return this book before deleting it.' : 'Delete book';
  remove.onclick = async () => {
    if (!confirm(`Delete “${book.title}” from the catalog?`)) return;
    remove.disabled = true;
    try {
      await api(book._links.self.href, { method: 'DELETE' });
      if (currentBooks.length === 1 && page > 0) page--;
      await load();
      notice('Book removed.');
    } catch (error) { notice(error.message, true); remove.disabled = false; }
  };
  actions.append(loan, edit, remove);
  card.append(actions);
  return card;
}

async function load() {
  const sequence = ++requestSequence;
  notice('Loading your collection…');
  try {
    const data = await api(`/api/books?q=${encodeURIComponent(query)}&page=${page}&size=12`);
    if (sequence !== requestSequence) return;
    currentBooks = data.items;
    totalPages = data.totalPages;
    $('total').textContent = data.totalElements;
    $('available').textContent = currentBooks.filter((book) => !book.borrowed).length;
    $('borrowed').textContent = currentBooks.filter((book) => book.borrowed).length;
    $('books').replaceChildren(...currentBooks.map(renderCard));
    if (!currentBooks.length) $('books').append(element('div', 'empty', query ? 'No matching titles. Try another search.' : 'Your next chapter starts here. Add your first book to the shelf.'));
    $('previous').disabled = page === 0;
    $('next').disabled = page + 1 >= totalPages;
    $('page-info').textContent = `Page ${page + 1} of ${Math.max(totalPages, 1)}`;
    notice('');
  } catch (error) { if (sequence === requestSequence) notice(error.message, true); }
}

function openEditor(book) {
  $('book-form').reset();
  $('form-error').textContent = '';
  $('book-id').value = book?.id || '';
  $('dialog-title').textContent = book ? 'Edit book details' : 'Add to your shelf';
  for (const field of ['title', 'author', 'isbn']) $(field).value = book?.[field] || '';
  $('editor').showModal();
  $('title').focus();
}

$('add').onclick = () => openEditor();
$('close').onclick = () => $('editor').close();
$('search-form').onsubmit = (event) => { event.preventDefault(); query = $('query').value.trim(); page = 0; load(); };
$('previous').onclick = () => { if (page > 0) { page--; load(); } };
$('next').onclick = () => { if (page + 1 < totalPages) { page++; load(); } };
$('book-form').onsubmit = async (event) => {
  event.preventDefault();
  const id = $('book-id').value;
  $('save').disabled = true;
  $('form-error').textContent = '';
  try {
    await api(id ? `/api/books/${id}` : '/api/books', {
      method: id ? 'PUT' : 'POST',
      body: JSON.stringify(Object.fromEntries(['title', 'author', 'isbn'].map((field) => [field, $(field).value.trim()])))
    });
    $('editor').close();
    await load();
    notice(id ? 'Book details updated.' : 'A new chapter added to your shelf.');
  } catch (error) { $('form-error').textContent = error.message; }
  finally { $('save').disabled = false; }
};
load();
