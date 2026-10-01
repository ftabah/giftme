const state = { token: localStorage.getItem('giftme.token'), userId: localStorage.getItem('giftme.userId'), authMode: 'login', photoFile: null, image: null, profile: null, cropPreview: null, previewMode: 'friend', crop: { x: 0, y: 0, zoom: 1, dragging: false } };
const $ = (selector) => document.querySelector(selector);

function setMessage(selector, message, success = false) { const element = $(selector); element.textContent = message || ''; element.style.color = success ? 'var(--teal)' : ''; }
function headers(json = true) { const result = {}; if (json) result['Content-Type'] = 'application/json'; if (state.token) result.Authorization = `Bearer ${state.token}`; return result; }
async function request(url, options = {}) { const isFormData = options.body instanceof FormData; const response = await fetch(url, { ...options, headers: { ...headers(!isFormData), ...(options.headers || {}) } }); const text = await response.text(); let data = {}; try { data = text ? JSON.parse(text) : {}; } catch { data = { message: text }; } if (response.status === 401 || response.status === 403) { clearSession(); showAuth(); } if (!response.ok) { const error = new Error(data.detail || data.message || `Erro ${response.status}`); error.status = response.status; throw error; } return data; }
function authErrorMessage(error, registering) { if (!error.status) return 'Não foi possível conectar ao servidor. Confira se a aplicação está ativa.'; if (error.status === 400) return error.message === 'Erro 400' ? 'Confira os dados informados e tente novamente.' : error.message; if (error.status === 401) return 'E-mail ou senha inválidos. Confira os dados e tente novamente.'; if (error.status === 403) return registering ? 'Não foi possível criar a conta. Confira a configuração do servidor.' : 'E-mail ainda não verificado. Abra o link recebido por e-mail antes de entrar.'; if (error.status === 409 && registering) return 'Este e-mail já está cadastrado. Tente entrar ou recuperar a senha.'; if (error.status === 429) return 'Muitas tentativas. Aguarde um minuto e tente novamente.'; if (error.status >= 500) return 'O servidor não conseguiu concluir a solicitação. Confira o terminal para ver o erro.'; return error.message || 'Não foi possível concluir a solicitação.'; }
function showApp() { clearPrivateData(); $('#auth-view').classList.add('hidden'); $('#app-view').classList.remove('hidden'); $('#session-label').textContent = state.token ? 'sessão ativa' : ''; loadProfile(); loadFriends(); }
function showAuth() { clearPrivateData(); $('#app-view').classList.add('hidden'); $('#auth-view').classList.remove('hidden'); }
function clearPrivateData() {
	state.profile = null;
	state.image = null;
	state.photoFile = null;
	state.cropPreview = null;
	state.previewMode = 'friend';
	state.crop = { x: 0, y: 0, zoom: 1, dragging: false };
	$('#profile-form').reset();
	$('#custom-sizes').replaceChildren();
	$('#photo-input').value = '';
	$('#photo-editor').classList.add('hidden');
	$('#crop-canvas').getContext('2d').clearRect(0, 0, $('#crop-canvas').width, $('#crop-canvas').height);
	$('#search-form').reset();
	$('#search-results').innerHTML = '<p class="empty-state">Faça uma busca para encontrar perfis.</p>';
	$('#friend-list-results').innerHTML = '<p class="empty-state">Seus amigos aparecerão aqui.</p>';
	$('#friends-results').innerHTML = '<p class="empty-state">Nenhuma solicitação carregada.</p>';
	$('#self-preview').innerHTML = '<p class="empty-state">Salve seu perfil para ver a prévia.</p>';
	$('#preview-note').textContent = '';
	document.querySelectorAll('.nav-item').forEach((item, index) => item.classList.toggle('active', index === 0));
	document.querySelectorAll('.content-section').forEach(section => section.classList.toggle('hidden', section.id !== 'profile-section'));
	document.querySelectorAll('.preview-tab').forEach((tab, index) => tab.classList.toggle('active', index === 0));
	setMessage('#profile-message', '');
	setPendingRequestCount(0);
}
function clearSession() { localStorage.removeItem('giftme.token'); localStorage.removeItem('giftme.userId'); state.token = null; state.userId = null; clearPrivateData(); }
function currentUserId() { if (state.userId) return state.userId; try { const payload = state.token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'); state.userId = JSON.parse(atob(payload)).sub; return state.userId; } catch { return null; } }
function setPendingRequestCount(count) { const item = $('[data-section="friends-section"]'); const indicator = $('#pending-request-count'); item.classList.toggle('has-pending', count > 0); item.setAttribute('aria-label', count ? `Solicitações: ${count} pendente${count === 1 ? '' : 's'} de resposta` : 'Solicitações'); indicator.textContent = count ? String(count) : ''; }
function customSizeRow(size = {}) { const row = document.createElement('div'); row.className = 'custom-row'; row.innerHTML = `<label>Nome<input class="custom-name" value="${size.name || ''}" required></label><label>Valor<input class="custom-value" value="${size.value || ''}" required></label><button type="button" class="remove-size" aria-label="Remover medida">×</button>`; row.querySelector('.remove-size').onclick = () => row.remove(); return row; }
function renderCustomSizes(sizes = []) { const list = $('#custom-sizes'); list.replaceChildren(...sizes.map(customSizeRow)); }
function drawCrop() {
	if (!state.image) return;
	const canvas = $('#crop-canvas');
	const context = canvas.getContext('2d');
	const scale = Math.max(canvas.width / state.image.naturalWidth, canvas.height / state.image.naturalHeight) * state.crop.zoom;
	const width = state.image.naturalWidth * scale;
	const height = state.image.naturalHeight * scale;
	const x = Math.min(0, Math.max(canvas.width - width, (canvas.width - width) / 2 + state.crop.x));
	const y = Math.min(0, Math.max(canvas.height - height, (canvas.height - height) / 2 + state.crop.y));
	context.fillStyle = '#cbd4cc';
	context.fillRect(0, 0, canvas.width, canvas.height);
	context.drawImage(state.image, x, y, width, height);
	state.cropPreview = canvas.toDataURL('image/jpeg', .9);
	renderSelfPreview();
}
function prepareCrop(file) {
	if (!['image/png', 'image/jpeg'].includes(file.type)) throw new Error('Escolha uma imagem PNG ou JPEG.');
	if (file.size > 2 * 1024 * 1024) throw new Error('A imagem original deve ter no máximo 2 MiB.');
	const image = new Image();
	image.onload = () => {
		const canvas = $('#crop-canvas');
		const stage = document.querySelector('.crop-stage');
		canvas.width = 200;
		canvas.height = 200;
		stage.style.width = '200px';
		stage.style.height = '200px';
		stage.style.aspectRatio = '1';
		state.image = image;
		state.photoFile = null;
		state.cropPreview = null;
		state.crop = { x: 0, y: 0, zoom: 1, dragging: false };
		$('#photo-editor').classList.remove('hidden');
		drawCrop();
	};
	image.src = URL.createObjectURL(file);
}
function croppedFile() { return new Promise(resolve => { $('#crop-canvas').toBlob(blob => resolve(blob ? new File([blob], 'giftme-crop.jpg', { type: 'image/jpeg' }) : null), 'image/jpeg', .9); }); }
function profilePayload() { return { name: $('#profile-name').value, age: Number($('#profile-age').value), height: $('#profile-height').value, shoeSize: $('#profile-shoe').value, waistSize: $('#profile-waist').value, shirtSize: $('#profile-shirt').value, interests: $('#profile-interests').value, customSizes: [...document.querySelectorAll('.custom-row')].map(row => ({ name: row.querySelector('.custom-name').value, value: row.querySelector('.custom-value').value })) }; }
function renderSelfPreview(profile = state.profile) { if (!profile) return; const photoSource = state.cropPreview || (profile.photoBase64 ? `data:${profile.photoMediaType};base64,${profile.photoBase64}` : null); const photo = photoSource ? `<img class="preview-photo" src="${photoSource}" alt="Foto do perfil">` : ''; const details = state.previewMode === 'friend' ? `<p>${profile.age} anos · ${profile.height}</p><p>Calçado ${profile.shoeSize} · cintura ${profile.waistSize} · torso/camiseta ${profile.shirtSize}</p>${(profile.customSizes || []).length ? `<p>${profile.customSizes.map(size => `${size.name}: ${size.value}`).join(' · ')}</p>` : ''}${profile.interests ? `<p><strong>Interesses:</strong> ${profile.interests}</p>` : ''}` : ''; $('#self-preview').innerHTML = `${photo}<h3>${profile.name}</h3>${details}`; $('#preview-note').textContent = state.previewMode === 'friend' ? 'Visão de amigo: medidas, interesses e foto ficam visíveis após a amizade ser aceita.' : 'Visão de desconhecido: somente seu nome e sua foto ficam visíveis.'; }
async function loadProfile() { try { const profile = await request('/api/profiles/me'); state.profile = profile; $('#profile-name').value = profile.name; $('#profile-age').value = profile.age; $('#profile-height').value = profile.height; $('#profile-shoe').value = profile.shoeSize; $('#profile-waist').value = profile.waistSize; $('#profile-shirt').value = profile.shirtSize; $('#profile-interests').value = profile.interests || ''; renderCustomSizes(profile.customSizes); renderSelfPreview(profile); } catch (error) { if (!error.message.includes('404')) setMessage('#profile-message', error.message); } }
function renderProfileCard(profile, action = true) { const visibleDetails = profile.height ? `<p>${profile.age} anos · ${profile.height}<br>Calçado ${profile.shoeSize} · cintura ${profile.waistSize} · camisa ${profile.shirtSize}</p>` : '<p>Nome e foto visíveis até a amizade ser aceita.</p>'; const photo = profile.photoBase64 ? `<img class="preview-photo" src="data:${profile.photoMediaType};base64,${profile.photoBase64}" alt="Foto do perfil">` : ''; const card = document.createElement('article'); card.className = 'result-card'; card.innerHTML = `${photo}<h3>${profile.name}</h3>${visibleDetails}${action ? '<button class="outline-button friend-button">Enviar solicitação +</button>' : ''}`; if (action) card.querySelector('.friend-button').onclick = async () => { try { await request('/api/friendships/requests', { method: 'POST', body: JSON.stringify({ recipientId: profile.userId }) }); card.querySelector('.friend-button').textContent = 'Solicitação enviada'; } catch (error) { setMessage('#profile-message', error.message); } }; return card; }
async function searchProfiles(event) { event.preventDefault(); const results = $('#search-results'); results.innerHTML = '<p class="empty-state">Buscando...</p>'; try { const profiles = await request(`/api/profiles/search?q=${encodeURIComponent($('#search-query').value)}`); results.replaceChildren(...(profiles.length ? profiles.map(profile => renderProfileCard(profile)) : [Object.assign(document.createElement('p'), { className: 'empty-state', textContent: 'Nenhum perfil encontrado.' })])); } catch (error) { results.innerHTML = `<p class="empty-state">${error.message}</p>`; } }
function renderFriendCard(friend) {
	const card = document.createElement('article');
	card.className = 'result-card';
	const profile = friend.counterpartProfile;
	if (!profile) {
		const title = document.createElement('h3');
		title.textContent = friend.counterpartEmail || 'Contato';
		const empty = document.createElement('p');
		empty.textContent = 'Esse amigo ainda não salvou o perfil.';
		card.append(title, empty);
		return card;
	}
	if (profile.photoBase64) {
		const photo = document.createElement('img');
		photo.className = 'preview-photo';
		photo.src = `data:${profile.photoMediaType};base64,${profile.photoBase64}`;
		photo.alt = `Foto de ${profile.name}`;
		card.append(photo);
	}
	const title = document.createElement('h3');
	title.textContent = profile.name;
	card.append(title);
	if (friend.counterpartEmail) {
		const email = document.createElement('p');
		email.className = 'muted';
		email.textContent = friend.counterpartEmail;
		card.append(email);
	}
	const ageAndHeight = document.createElement('p');
	ageAndHeight.textContent = `${profile.age} anos · ${profile.height}`;
	card.append(ageAndHeight);
	const standardSizes = document.createElement('p');
	standardSizes.textContent = `Calçado ${profile.shoeSize} · cintura ${profile.waistSize} · torso/camiseta ${profile.shirtSize}`;
	card.append(standardSizes);
	if (profile.customSizes?.length) {
		const customSizes = document.createElement('p');
		customSizes.textContent = profile.customSizes.map(size => `${size.name}: ${size.value}`).join(' · ');
		card.append(customSizes);
	}
	if (profile.interests) {
		const interests = document.createElement('p');
		interests.textContent = `Interesses: ${profile.interests}`;
		card.append(interests);
	}
	return card;
}
function renderRequestCard(friend, userId) {
	const incoming = friend.recipientId === userId;
	const card = document.createElement('article');
	card.className = 'result-card';
	card.innerHTML = `<h3>${friend.counterpartEmail || 'Contato'}</h3><p>${incoming ? 'Solicitação recebida' : 'Aguardando resposta'}</p>${incoming ? '<button class="outline-button decision-button" data-decision="ACCEPT">Aceitar</button> <button class="outline-button decision-button" data-decision="REJECT">Rejeitar</button>' : ''}`;
	card.querySelectorAll('.decision-button').forEach(button => button.onclick = async () => {
		try {
			await request(`/api/friendships/requests/${friend.id}/decision`, { method: 'POST', body: JSON.stringify({ decision: button.dataset.decision }) });
			loadFriends();
		} catch (error) {
			setMessage('#profile-message', error.message);
		}
	});
	return card;
}
async function loadFriends() {
	const friendResults = $('#friend-list-results');
	const requestResults = $('#friends-results');
	try {
		const relationships = await request('/api/friendships/requests');
		const userId = currentUserId();
		const accepted = relationships.filter(friend => friend.status === 'ACCEPTED');
		const pending = relationships.filter(friend => friend.status === 'PENDING');
		const pendingCount = pending.filter(friend => friend.recipientId === userId).length;
		setPendingRequestCount(pendingCount);
		friendResults.replaceChildren(...(accepted.length ? accepted.map(renderFriendCard) : [Object.assign(document.createElement('p'), { className: 'empty-state', textContent: 'Você ainda não tem amigos no GiftMe.' })]));
		requestResults.replaceChildren(...(pending.length ? pending.map(friend => renderRequestCard(friend, userId)) : [Object.assign(document.createElement('p'), { className: 'empty-state', textContent: 'Nenhuma solicitação pendente.' })]));
	} catch (error) {
		setPendingRequestCount(0);
		friendResults.innerHTML = `<p class="empty-state">${error.message}</p>`;
		requestResults.innerHTML = `<p class="empty-state">${error.message}</p>`;
	}
}
$('#auth-form').onsubmit = async (event) => { event.preventDefault(); setMessage('#auth-message', ''); const registering = state.authMode === 'register'; const action = registering ? 'Cadastro' : 'Login'; console.info(`[GiftMe] ${action} iniciado`); try { const endpoint = registering ? '/api/auth/register' : '/api/auth/login'; const data = await request(endpoint, { method: 'POST', body: JSON.stringify({ email: $('#auth-email').value, password: $('#auth-password').value }) }); if (registering) { $('#auth-password').value = ''; document.querySelector('[data-auth-mode="login"]').click(); setMessage('#auth-message', data.emailVerificationRequired ? 'Conta criada. Verifique seu e-mail e confirme o endereço antes de entrar.' : 'Conta criada. Agora você pode entrar com seu e-mail e senha.', true); console.info('[GiftMe] Cadastro concluído'); return; } state.token = data.token; state.userId = data.userId; localStorage.setItem('giftme.token', data.token); localStorage.setItem('giftme.userId', data.userId); console.info('[GiftMe] Login concluído'); showApp(); } catch (error) { console.error(`[GiftMe] ${action} falhou`, { status: error.status ?? 'rede', message: error.message }); setMessage('#auth-message', authErrorMessage(error, registering)); } };
document.querySelectorAll('[data-auth-mode]').forEach(tab => tab.onclick = () => { state.authMode = tab.dataset.authMode; document.querySelectorAll('.tab').forEach(item => item.classList.toggle('active', item === tab)); $('#auth-submit-label').textContent = state.authMode === 'login' ? 'Entrar na conta' : 'Criar minha conta'; });
document.querySelectorAll('[data-section]').forEach(item => item.onclick = () => { document.querySelectorAll('.nav-item').forEach(nav => nav.classList.toggle('active', nav === item)); document.querySelectorAll('.content-section').forEach(section => section.classList.toggle('hidden', section.id !== item.dataset.section)); if (['friend-list-section', 'friends-section'].includes(item.dataset.section)) loadFriends(); });
document.querySelectorAll('[data-preview-mode]').forEach(tab => tab.onclick = () => { state.previewMode = tab.dataset.previewMode; document.querySelectorAll('.preview-tab').forEach(item => item.classList.toggle('active', item === tab)); renderSelfPreview(); });
$('#add-size').onclick = () => $('#custom-sizes').appendChild(customSizeRow());
$('#forgot-password').onclick = async () => { const email = $('#auth-email').value.trim(); if (!email) { setMessage('#auth-message', 'Digite o e-mail da conta para recuperar a senha.'); $('#auth-email').focus(); return; } setMessage('#auth-message', ''); try { await request('/api/auth/password/forgot', { method: 'POST', body: JSON.stringify({ email }) }); setMessage('#auth-message', 'Se o e-mail estiver cadastrado, enviaremos instruções de recuperação.', true); } catch (error) { setMessage('#auth-message', 'Não foi possível solicitar a recuperação agora.'); } };
const resetToken = new URLSearchParams(window.location.search).get('token');
if (window.location.pathname === '/reset-password') { $('#auth-view').classList.add('hidden'); $('#reset-view').classList.remove('hidden'); if (!resetToken) setMessage('#reset-message', 'Link de redefinição ausente ou inválido. Solicite um novo e-mail.'); }
$('#reset-form').onsubmit = async (event) => { event.preventDefault(); const password = $('#reset-password').value; if (password !== $('#reset-password-confirm').value) { setMessage('#reset-message', 'As senhas não coincidem.'); return; } if (!resetToken) { setMessage('#reset-message', 'Link de redefinição ausente ou inválido. Solicite um novo e-mail.'); return; } try { await request('/api/auth/password/reset', { method: 'POST', body: JSON.stringify({ token: resetToken, password }) }); $('#reset-form').reset(); setMessage('#reset-message', 'Senha redefinida. Você já pode voltar ao login.', true); } catch (error) { setMessage('#reset-message', error.message.includes('400') ? 'O link é inválido, expirou ou já foi usado. Solicite outro e-mail.' : error.message); } };
$('#photo-input').onchange = () => { const file = $('#photo-input').files[0]; if (!file) return; try { prepareCrop(file); setMessage('#crop-message', 'Ajuste o enquadramento e confirme o recorte.', true); } catch (error) { $('#photo-input').value = ''; setMessage('#profile-message', error.message); } };
const cropCanvas = $('#crop-canvas'); cropCanvas.onpointerdown = event => { state.crop.dragging = true; state.crop.startX = event.clientX; state.crop.startY = event.clientY; state.crop.originX = state.crop.x; state.crop.originY = state.crop.y; cropCanvas.setPointerCapture(event.pointerId); cropCanvas.style.cursor = 'grabbing'; }; cropCanvas.onpointermove = event => { if (!state.crop.dragging) return; const bounds = cropCanvas.getBoundingClientRect(); const scaleX = cropCanvas.width / bounds.width; const scaleY = cropCanvas.height / bounds.height; state.crop.x = state.crop.originX + (event.clientX - state.crop.startX) * scaleX; state.crop.y = state.crop.originY + (event.clientY - state.crop.startY) * scaleY; drawCrop(); }; cropCanvas.onpointerup = event => { state.crop.dragging = false; cropCanvas.releasePointerCapture(event.pointerId); cropCanvas.style.cursor = 'grab'; }; cropCanvas.onwheel = event => { event.preventDefault(); state.crop.zoom = Math.max(1, Math.min(3, state.crop.zoom + (event.deltaY < 0 ? .05 : -.05))); drawCrop(); };
$('#apply-crop').onclick = async () => { state.photoFile = await croppedFile(); setMessage('#crop-message', 'Recorte pronto para salvar.', true); };
$('#profile-form').onsubmit = async (event) => { event.preventDefault(); try { await request('/api/profiles/me', { method: 'PUT', body: JSON.stringify(profilePayload()) }); if (state.image && !state.photoFile) state.photoFile = await croppedFile(); if (state.photoFile) { const form = new FormData(); form.append('file', state.photoFile); await request('/api/profiles/me/photo', { method: 'PUT', body: form, headers: { Authorization: `Bearer ${state.token}` } }); } setMessage('#profile-message', 'Perfil salvo.', true); await loadProfile(); } catch (error) { setMessage('#profile-message', error.message); } };
$('#search-form').onsubmit = searchProfiles; $('#refresh-friends').onclick = loadFriends; $('#logout-button').onclick = async () => { try { await request('/api/auth/logout', { method: 'POST', body: JSON.stringify({}) }); } catch (error) { console.warn('[GiftMe] Logout do servidor falhou; a sessão local será encerrada', error.message); } finally { clearSession(); showAuth(); } };
const emailVerified = new URLSearchParams(window.location.search).get('emailVerified') === '1';
if (emailVerified) { clearSession(); document.querySelector('[data-auth-mode="login"]').click(); setMessage('#auth-message', 'E-mail confirmado com sucesso. Agora você pode entrar.', true); history.replaceState(null, '', '/'); }
else if (state.token) showApp();