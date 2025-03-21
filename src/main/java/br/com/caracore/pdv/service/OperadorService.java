package br.com.caracore.pdv.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.caracore.pdv.model.Loja;
import br.com.caracore.pdv.model.Operador;
import br.com.caracore.pdv.model.types.TipoOperador;
import br.com.caracore.pdv.repository.LojaRepository;
import br.com.caracore.pdv.repository.OperadorRepository;
import br.com.caracore.pdv.repository.filter.OperadorFilter;
import br.com.caracore.pdv.service.exception.AdminExistenteException;
import br.com.caracore.pdv.service.exception.EmailInvalidoException;
import br.com.caracore.pdv.service.exception.LoginExistenteException;
import br.com.caracore.pdv.service.exception.SenhaInvalidaException;
import br.com.caracore.pdv.util.Util;

@Service
public class OperadorService {
	
	@Autowired
	private OperadorRepository operadorRepository;

	@Autowired
	private LojaRepository lojaRepository;
	
	/**
	 * Método para pesquisar operador por nome
	 * 
	 * @param login
	 * @return
	 */
	public Operador pesquisarPorNome(String login) {
		Operador operador = null;
		List<Operador> operadores = pesquisarPorLogin(login);
		if (Util.validar(operadores)) {
			if (operadores.size() == 1) {
				for (Operador _operador : operadores) {
					operador = _operador;
				}
			}
		}
		return operador;
	}

	/**
	 * Método externo para pesquisar operadores por filtro
	 * 
	 * @param filtro
	 * @return
	 */
	public List<Operador> pesquisar(OperadorFilter filtro) {
		String nome = filtro.getNome() == null ? "%" : filtro.getNome();
		return operadorRepository.findByNomeContainingIgnoreCase(nome);
	}
	
	/**
	 * Método extrno para buscar todos os operadores
	 * 
	 * @return
	 */
	public List<Operador> buscarTodos() {
		return operadorRepository.findAll();
	}
	
	public Operador salvar(Operador operador) {
		boolean verificar = false;
		String login = operador.getNome();
		String email = operador.getEmail();
		String repetirEmail = operador.getRepetirEmail();
		String senha = operador.getSenha();
		String repetirSenha = operador.getRepetirSenha();
		if (operador.getPerfil().equals(TipoOperador.ADMINISTRADOR)) {
			Operador admin = buscarAdministrador();
			if (Util.validar(admin)) {
				throw new AdminExistenteException("Administrador já existente!");
			}
		}
		verificar = verificarLogin(login);
		if (verificar) {
			throw new LoginExistenteException("Login já existente!");
		}
		if (!repetirSenha.equals(senha)) {
			throw new SenhaInvalidaException("Senha inválida!");
		}
		if (!repetirEmail.equals(email)) {
			throw new EmailInvalidoException("Email inválido!");
		}
		return operadorRepository.save(operador);
	}
	
	public Operador buscar(String login) {
		Operador operador = null;
		operador = operadorRepository.findByNome(login);
		return operador;
	}
	
	public boolean verificarExistenciaLogin(String login) {
		boolean existe = false;
		Operador operador = operadorRepository.findByNome(login);
		if (Util.validar(operador)) {
			existe = true;
		}
		return existe;
	}
	
	public boolean verificarExistenciaEmail(String email) {
		boolean existe = false;
		List<Operador> lista = operadorRepository.findByEmail(email);
		if (Util.validar(lista)) {
			existe = true;
		}
		return existe;
	}
	
	public void excluirPorLogin(String login) {
		List<Operador> listar = pesquisarPorLogin(login);
		Operador operador = retornarOperador(listar);
		excluir(operador.getCodigo());
	}

	public List<Loja> buscarLojas() {
		List<Loja> lista = lojaRepository.findAll();
		if (!Util.validar(lista)) {
			lista = Util.criarListaDeLojas();
		}
		return lista;
	}
	
	public List<Operador> pesquisarPorLogin(String login) {
		return operadorRepository.findByNomeContainingIgnoreCase(login);
	}

	public void excluir(Long codigo) {
		operadorRepository.delete(codigo);
	}

	public Operador pesquisarPorId(Long codigo) {
		return operadorRepository.findOne(codigo);
	}
	
	private Operador retornarOperador(List<Operador> listar) {
		Operador result = null;
		if (listar != null && listar.size() == 1) {
			for(Operador operador : listar) {
				result = operador;
			}
		}
		return result;
	}
	
	private Operador buscarAdministrador() {
		Operador operadorAdmin = null;
		TipoOperador perfil = TipoOperador.ADMINISTRADOR;
		operadorAdmin = operadorRepository.findByPerfil(perfil);
		return operadorAdmin;
	}
	
	private boolean verificarLogin(String login) {
		boolean result = false;
		if (login != null && !login.equals("")) {
			List<Operador> listar = pesquisarPorLogin(login);
			Operador operador = retornarOperador(listar);
			if (operador != null) {
				result = true;
			}
		}
		return result;
	}
}
