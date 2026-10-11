/*
 * 솝모담 어드민 화면 공통 스크립트.
 * 화면 틀은 서버(Thymeleaf)가 그리고, 데이터는 admin-key 헤더로 /api/v1/admin/sopmodam API 를 호출해 채운다.
 * 질문·주제처럼 입력된 값은 innerHTML 에 넣지 않고 textContent 로만 넣는다. (admin-key 가 localStorage 에 있어 XSS 에 취약하다)
 * API 일시는 오프셋 없는 KST LocalDateTime 문자열이므로 Date 로 시간대를 바꾸지 않고 문자열로만 다룬다.
 */
const SopmodamAdmin = (() => {
    const ADMIN_KEY_STORAGE = 'adminKey';
    const API_BASE = '/api/v1/admin/sopmodam';
    const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토'];

    const PHASES = {
        SCHEDULED: { label: '예정', tagClass: 'mds-tag-default' },
        VOTING: { label: '투표 중', tagClass: 'mds-tag-primary' },
        ANSWERING: { label: '답변 모집', tagClass: 'mds-tag-secondary' },
        REVEALED: { label: '공개', tagClass: 'mds-tag-default-solid' },
    };

    // 고정 마크업만 담는다. 데이터를 섞지 않는다
    const ICONS = {
        danger: '<svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><circle cx="10" cy="10" r="8.25" stroke="currentColor" stroke-width="1.5"/><path d="M10 5.75v5.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/><circle cx="10" cy="14" r="1" fill="currentColor"/></svg>',
        information: '<svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><circle cx="10" cy="10" r="8.25" stroke="currentColor" stroke-width="1.5"/><path d="M10 8.75v5.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/><circle cx="10" cy="6" r="1" fill="currentColor"/></svg>',
    };

    class ApiError extends Error {
        constructor(status, message) {
            super(message);
            this.status = status;
        }
    }

    function getAdminKey() {
        return localStorage.getItem(ADMIN_KEY_STORAGE);
    }

    function redirectToLogin() {
        localStorage.removeItem(ADMIN_KEY_STORAGE);
        location.replace('/admin?redirect=' + encodeURIComponent(location.pathname));
    }

    // 키가 없으면 로그인으로 보내고 false 를 돌려준다
    function requireLogin() {
        if (getAdminKey()) {
            return true;
        }
        redirectToLogin();
        return false;
    }

    // 성공은 래퍼 없는 본문, 실패는 { message } 형식 (GlobalExceptionHandler)
    async function api(method, path, body) {
        const headers = { 'admin-key': getAdminKey() };
        if (body !== undefined) {
            headers['Content-Type'] = 'application/json';
        }

        let response;
        try {
            response = await fetch(API_BASE + path, {
                method,
                headers,
                body: body === undefined ? undefined : JSON.stringify(body),
            });
        } catch (error) {
            throw new ApiError(0, '네트워크 오류가 발생했어요. 잠시 후 다시 시도해 주세요.');
        }

        if (response.status === 403) {
            redirectToLogin();
            throw new ApiError(403, '관리자 키가 올바르지 않아요. 다시 로그인해 주세요.');
        }
        if (response.status === 204) {
            return null;
        }

        const data = await response.json().catch(() => null);
        if (!response.ok) {
            throw new ApiError(response.status, toDisplayMessage(data?.message) ?? `요청에 실패했어요. (${response.status})`);
        }
        return data;
    }

    // 예외 메시지 앞에 붙는 "BadRequestException: " 같은 예외 이름을 화면에서는 뗀다
    function toDisplayMessage(message) {
        return message?.replace(/^\w+Exception: /, '');
    }

    // "2026-10-10T00:00:00" → "2026.10.10 (토) 00:00"
    function formatDateTime(value) {
        if (!value) {
            return '-';
        }
        const [date, time] = value.split('T');
        const [year, month, day] = date.split('-').map(Number);
        const weekday = WEEKDAYS[new Date(year, month - 1, day).getDay()];
        return `${date.replaceAll('-', '.')} (${weekday}) ${time.slice(0, 5)}`;
    }

    // API 값 → datetime-local 입력값 ("2026-10-10T00:00")
    function toInputValue(value) {
        return value ? value.slice(0, 16) : '';
    }

    // datetime-local 입력값 → API 값 ("2026-10-10T00:00:00")
    function toRequestValue(inputValue) {
        if (!inputValue) {
            return null;
        }
        return inputValue.length === 16 ? `${inputValue}:00` : inputValue;
    }

    function h(tag, props = {}, children = []) {
        const element = document.createElement(tag);
        const { className, text, attrs, on } = props;
        if (className) {
            element.className = className;
        }
        if (text !== undefined && text !== null) {
            element.textContent = String(text);
        }
        Object.entries(attrs ?? {}).forEach(([name, value]) => {
            if (value === true) {
                element.setAttribute(name, '');
            } else if (value !== false && value !== null && value !== undefined) {
                element.setAttribute(name, value);
            }
        });
        Object.entries(on ?? {}).forEach(([event, handler]) => element.addEventListener(event, handler));
        [children].flat(Infinity)
            .filter((child) => child !== null && child !== undefined && child !== false)
            .forEach((child) => element.append(child instanceof Node ? child : String(child)));
        return element;
    }

    function phaseTag(phase) {
        const { label, tagClass } = PHASES[phase] ?? { label: phase, tagClass: 'mds-tag-default' };
        return h('span', { className: `mds-tag ${tagClass}`, text: label });
    }

    function callout(type, message) {
        const icon = h('span', { className: 'mds-callout-icon' });
        icon.innerHTML = ICONS[type];
        return h('div', { className: `mds-callout mds-callout-${type}`, attrs: { role: type === 'danger' ? 'alert' : 'status' } }, [
            icon,
            h('p', { className: 'mds-callout-body', text: message }),
        ]);
    }

    function showMessage(type, message) {
        const container = document.getElementById('page-alert');
        container.replaceChildren(callout(type, message));
        container.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
    }

    function showError(message) {
        showMessage('danger', message);
    }

    function showNotice(message) {
        showMessage('information', message);
    }

    function clearMessage() {
        document.getElementById('page-alert').replaceChildren();
    }

    // 요청 중에는 버튼을 막아 중복 요청을 막는다
    async function withPending(button, pendingText, task) {
        const originalText = button.textContent;
        button.disabled = true;
        button.textContent = pendingText;
        try {
            return await task();
        } finally {
            button.disabled = false;
            button.textContent = originalText;
        }
    }

    // .mds-field 단위 에러 표시. 헬퍼의 원래 문구는 data-default 로 되살린다
    function setFieldError(field, message) {
        const helper = field.querySelector('.mds-field-helper');
        field.classList.add('is-error');
        if (helper) {
            helper.dataset.default ??= helper.textContent;
            helper.textContent = message;
        }
    }

    function clearFieldError(field) {
        const helper = field.querySelector('.mds-field-helper');
        field.classList.remove('is-error');
        if (helper && helper.dataset.default !== undefined) {
            helper.textContent = helper.dataset.default;
        }
    }

    function clearFieldErrors(root) {
        root.querySelectorAll('.mds-field.is-error').forEach(clearFieldError);
    }

    // 값을 고치면 그 필드의 에러 표시를 바로 지운다. 나중에 추가되는 입력칸도 위임으로 처리한다
    function bindErrorReset(form) {
        form.addEventListener('input', (event) => {
            const field = event.target.closest('.mds-field.is-error');
            if (field) {
                clearFieldError(field);
            }
        });
    }

    // 필드 이름 → 메시지. 첫 번째 에러 필드로 포커스를 옮긴다
    function applyFieldErrors(form, errors) {
        clearFieldErrors(form);
        const entries = Object.entries(errors);
        entries.forEach(([name, message]) => {
            const field = form.querySelector(`[data-field="${name}"]`);
            if (field) {
                setFieldError(field, message);
            }
        });
        if (entries.length > 0) {
            form.querySelector(`[data-field="${entries[0][0]}"] .mds-input`)?.focus();
        }
        return entries.length === 0;
    }

    // data-counter-for 가 붙은 글자 수 표시를 입력값과 맞춘다
    function bindCounters(form) {
        form.querySelectorAll('[data-counter-for]').forEach((counter) => {
            const input = form.elements[counter.dataset.counterFor];
            const update = () => {
                counter.textContent = `${input.value.length}/${input.maxLength}`;
            };
            input.addEventListener('input', update);
            update();
        });
    }

    // fragments.html 의 round-fields 입력값. 일시는 입력값(YYYY-MM-DDTHH:mm) 그대로 돌려준다
    function readRoundFields(form) {
        const value = (name) => form.elements[name].value;
        return {
            generation: value('generation') === '' ? null : Number(value('generation')),
            eventName: value('eventName').trim(),
            voteStartAt: value('voteStartAt'),
            voteEndAt: value('voteEndAt'),
            answerEndAt: value('answerEndAt'),
        };
    }

    function fillRoundFields(form, round) {
        form.elements.generation.value = round.generation;
        form.elements.eventName.value = round.eventName;
        form.elements.voteStartAt.value = toInputValue(round.voteStartAt);
        form.elements.voteEndAt.value = toInputValue(round.voteEndAt);
        form.elements.answerEndAt.value = toInputValue(round.answerEndAt);
        form.elements.eventName.dispatchEvent(new Event('input'));
    }

    function toRoundRequest(fields) {
        return {
            generation: fields.generation,
            eventName: fields.eventName,
            voteStartAt: toRequestValue(fields.voteStartAt),
            voteEndAt: toRequestValue(fields.voteEndAt),
            answerEndAt: toRequestValue(fields.answerEndAt),
        };
    }

    // 회차 생성·수정 공통 검증. 서버 검증과 같은 규칙이며, 실패한 필드 이름과 메시지를 돌려준다
    function validateRoundFields({ generation, eventName, voteStartAt, voteEndAt, answerEndAt }) {
        const errors = {};
        if (!Number.isInteger(generation) || generation <= 0) {
            errors.generation = '기수는 양수로 입력해 주세요.';
        }
        if (!eventName) {
            errors.eventName = '주제를 입력해 주세요.';
        }
        if (!voteStartAt) {
            errors.voteStartAt = '투표 시작 시각을 입력해 주세요.';
        }
        if (!voteEndAt) {
            errors.voteEndAt = '투표 마감 시각을 입력해 주세요.';
        }
        if (!answerEndAt) {
            errors.answerEndAt = '답변 마감 시각을 입력해 주세요.';
        }
        if (voteStartAt && voteEndAt && voteStartAt >= voteEndAt) {
            errors.voteEndAt = '투표 마감은 투표 시작보다 늦어야 해요.';
        }
        if (voteEndAt && answerEndAt && voteEndAt >= answerEndAt) {
            errors.answerEndAt = '답변 마감은 투표 마감보다 늦어야 해요.';
        }
        return errors;
    }

    // 네이티브 confirm() 대신 MDS Dialog 모양의 확인 창. 확인을 누르면 true
    function confirmDialog({ title, description, confirmText = '확인', danger = false }) {
        return new Promise((resolve) => {
            let confirmed = false;
            const dialog = h('dialog', { className: 'mds-dialog' }, [
                h('h3', { className: 'mds-dialog-title', text: title }),
                description ? h('p', { className: 'mds-dialog-description', text: description }) : null,
                h('div', { className: 'mds-dialog-actions' }, [
                    h('button', {
                        className: 'mds-btn mds-btn-md mds-btn-secondary',
                        text: '취소',
                        attrs: { type: 'button' },
                        on: { click: () => dialog.close() },
                    }),
                    h('button', {
                        className: `mds-btn mds-btn-md ${danger ? 'mds-btn-danger' : 'mds-btn-primary'}`,
                        text: confirmText,
                        attrs: { type: 'button' },
                        on: {
                            click: () => {
                                confirmed = true;
                                dialog.close();
                            },
                        },
                    }),
                ]),
            ]);
            dialog.addEventListener('close', () => {
                dialog.remove();
                resolve(confirmed);
            });
            document.body.append(dialog);
            dialog.showModal();
        });
    }

    return {
        ApiError,
        requireLogin,
        api,
        formatDateTime,
        toInputValue,
        toRequestValue,
        h,
        phaseTag,
        callout,
        showError,
        showNotice,
        clearMessage,
        withPending,
        setFieldError,
        clearFieldErrors,
        applyFieldErrors,
        bindErrorReset,
        bindCounters,
        readRoundFields,
        fillRoundFields,
        toRoundRequest,
        validateRoundFields,
        confirmDialog,
    };
})();
