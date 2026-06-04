import { useEffect, useMemo, useState } from 'react';
import {
  Loader2,
  Mail,
  Pencil,
  Phone,
  Plus,
  RefreshCw,
  Save,
  Search,
  Trash2,
  UserRound,
  X
} from 'lucide-react';

const emptyForm = {
  nome: '',
  email: '',
  telefone: ''
};

function App() {
  const [clientes, setClientes] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [feedback, setFeedback] = useState(null);

  const filteredClientes = useMemo(() => {
    const term = search.trim().toLowerCase();

    if (!term) {
      return clientes;
    }

    return clientes.filter((cliente) => {
      return [cliente.nome, cliente.email, cliente.telefone]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(term));
    });
  }, [clientes, search]);

  const stats = useMemo(() => {
    const withPhone = clientes.filter((cliente) => cliente.telefone).length;
    const recent = clientes
      .slice()
      .sort((a, b) => new Date(b.criadoEm ?? 0) - new Date(a.criadoEm ?? 0))[0];

    return {
      total: clientes.length,
      withPhone,
      recentName: recent?.nome ?? 'Nenhum cliente'
    };
  }, [clientes]);

  useEffect(() => {
    loadClientes();
  }, []);

  async function loadClientes() {
    try {
      setLoading(true);
      setFeedback(null);
      const response = await fetch('/clientes');

      if (!response.ok) {
        throw new Error('Nao foi possivel carregar os clientes.');
      }

      const data = await response.json();
      setClientes(data);
    } catch (error) {
      setFeedback({ type: 'error', message: error.message });
    } finally {
      setLoading(false);
    }
  }

  function updateField(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  function startEditing(cliente) {
    setEditingId(cliente.id);
    setForm({
      nome: cliente.nome ?? '',
      email: cliente.email ?? '',
      telefone: cliente.telefone ?? ''
    });
    setFeedback(null);
  }

  function resetForm() {
    setEditingId(null);
    setForm(emptyForm);
  }

  async function handleSubmit(event) {
    event.preventDefault();

    try {
      setSaving(true);
      setFeedback(null);

      const response = await fetch(editingId ? `/clientes/${editingId}` : '/clientes', {
        method: editingId ? 'PUT' : 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          nome: form.nome.trim(),
          email: form.email.trim(),
          telefone: form.telefone.trim()
        })
      });

      if (!response.ok) {
        const message = await readErrorMessage(response);
        throw new Error(message);
      }

      resetForm();
      await loadClientes();
      setFeedback({
        type: 'success',
        message: editingId ? 'Cliente atualizado.' : 'Cliente cadastrado.'
      });
    } catch (error) {
      setFeedback({ type: 'error', message: error.message });
    } finally {
      setSaving(false);
    }
  }

  async function deleteCliente(id) {
    const shouldDelete = window.confirm('Excluir este cliente?');

    if (!shouldDelete) {
      return;
    }

    try {
      setFeedback(null);
      const response = await fetch(`/clientes/${id}`, { method: 'DELETE' });

      if (!response.ok) {
        const message = await readErrorMessage(response);
        throw new Error(message);
      }

      setClientes((current) => current.filter((cliente) => cliente.id !== id));
      if (editingId === id) {
        resetForm();
      }
      setFeedback({ type: 'success', message: 'Cliente removido.' });
    } catch (error) {
      setFeedback({ type: 'error', message: error.message });
    }
  }

  return (
    <main className="app-shell">
      <section className="workspace">
        <aside className="sidebar" aria-label="Resumo de clientes">
          <div>
            <p className="eyebrow">MeuStudio</p>
            <h1>Cadastro de clientes</h1>
            <p className="intro">
              Gerencie contatos, mantenha dados essenciais atualizados e acompanhe a base em um painel direto.
            </p>
          </div>

          <div className="metric-grid">
            <Metric label="Clientes" value={stats.total} />
            <Metric label="Com telefone" value={stats.withPhone} />
            <Metric label="Ultimo cadastro" value={stats.recentName} />
          </div>
        </aside>

        <section className="content-area">
          <form className="client-form" onSubmit={handleSubmit}>
            <div className="section-heading">
              <div>
                <p className="eyebrow">{editingId ? 'Editando' : 'Novo cadastro'}</p>
                <h2>{editingId ? 'Atualizar cliente' : 'Adicionar cliente'}</h2>
              </div>
              {editingId && (
                <button className="icon-button muted" type="button" onClick={resetForm} title="Cancelar edicao">
                  <X size={18} />
                </button>
              )}
            </div>

            <div className="form-grid">
              <label>
                <span>Nome</span>
                <div className="input-shell">
                  <UserRound size={18} />
                  <input
                    name="nome"
                    value={form.nome}
                    onChange={updateField}
                    placeholder="Nome completo"
                    maxLength={120}
                    required
                  />
                </div>
              </label>

              <label>
                <span>Email</span>
                <div className="input-shell">
                  <Mail size={18} />
                  <input
                    name="email"
                    type="email"
                    value={form.email}
                    onChange={updateField}
                    placeholder="cliente@email.com"
                    maxLength={160}
                    required
                  />
                </div>
              </label>

              <label>
                <span>Telefone</span>
                <div className="input-shell">
                  <Phone size={18} />
                  <input
                    name="telefone"
                    value={form.telefone}
                    onChange={updateField}
                    placeholder="(00) 00000-0000"
                    maxLength={20}
                  />
                </div>
              </label>
            </div>

            <div className="form-actions">
              <button className="primary-button" type="submit" disabled={saving}>
                {saving ? <Loader2 className="spin" size={18} /> : editingId ? <Save size={18} /> : <Plus size={18} />}
                {saving ? 'Salvando...' : editingId ? 'Salvar alteracoes' : 'Cadastrar cliente'}
              </button>
            </div>
          </form>

          <section className="table-section" aria-label="Lista de clientes">
            <div className="toolbar">
              <div className="search-box">
                <Search size={18} />
                <input
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                  placeholder="Buscar por nome, email ou telefone"
                />
              </div>

              <button className="icon-button" type="button" onClick={loadClientes} title="Atualizar lista">
                <RefreshCw size={18} />
              </button>
            </div>

            {feedback && <p className={`feedback ${feedback.type}`}>{feedback.message}</p>}

            <div className="client-list">
              {loading ? (
                <div className="empty-state">
                  <Loader2 className="spin" size={28} />
                  <span>Carregando clientes...</span>
                </div>
              ) : filteredClientes.length === 0 ? (
                <div className="empty-state">
                  <UserRound size={28} />
                  <span>Nenhum cliente encontrado.</span>
                </div>
              ) : (
                filteredClientes.map((cliente) => (
                  <article className="client-row" key={cliente.id}>
                    <div className="avatar">{getInitials(cliente.nome)}</div>
                    <div className="client-main">
                      <strong>{cliente.nome}</strong>
                      <span>{cliente.email}</span>
                    </div>
                    <div className="client-phone">{cliente.telefone || 'Sem telefone'}</div>
                    <div className="row-actions">
                      <button
                        className="icon-button muted"
                        type="button"
                        onClick={() => startEditing(cliente)}
                        title="Editar cliente"
                      >
                        <Pencil size={17} />
                      </button>
                      <button
                        className="icon-button danger"
                        type="button"
                        onClick={() => deleteCliente(cliente.id)}
                        title="Excluir cliente"
                      >
                        <Trash2 size={17} />
                      </button>
                    </div>
                  </article>
                ))
              )}
            </div>
          </section>
        </section>
      </section>
    </main>
  );
}

function Metric({ label, value }) {
  return (
    <div className="metric">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function getInitials(name = '') {
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0])
    .join('')
    .toUpperCase();
}

async function readErrorMessage(response) {
  try {
    const data = await response.json();
    return data.message || data.error || 'A requisicao falhou.';
  } catch {
    return 'A requisicao falhou.';
  }
}

export default App;
