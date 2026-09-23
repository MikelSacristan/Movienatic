package com.EnSe.Movienatic.service;

import com.EnSe.Movienatic.model.User;
import com.EnSe.Movienatic.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    public UserService(UserRepository userRepository) {
    }

    // @Transactional
    // public void rexistrarPedido(Long userId, String codigoPedido) {
    // // 1. Obter a entidade da base de datos (pasa a estar no estado "Managed")
    // user user = userRepository.findById(userId)
    // .orElseThrow(() -> new IllegalArgumentException("user non atopado"));

    // // 2. Modificar o grafo de obxectos en memoria
    // Pedido novoPedido = new Pedido();
    // novoPedido.setCodigo(codigoPedido);
    // user.addPedido(novoPedido);

    // // 3. Ao finalizar o método transaccional con éxito, o contexto de
    // persistencia
    // // executa o "flush" e sincroniza as mudanzas coa base de datos
    // automaticamente.
    // }
}
