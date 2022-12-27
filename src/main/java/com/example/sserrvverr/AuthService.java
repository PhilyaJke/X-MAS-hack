package com.example.sserrvverr;

import com.example.sserrvverr.entity.User;
import com.example.sserrvverr.repository.UserRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Base64;

@GrpcService
public class AuthService extends AuthenticationGrpc.AuthenticationImplBase {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void authenticate(AuthenticationOuterClass.AuthenticationRequest request,
                             StreamObserver<AuthenticationOuterClass.AuthenticationResponse> responseObserver) {

        String[] buffers = request.getToken().split("\\.");
        Base64.Decoder decoder = Base64.getDecoder();
        String payload = new String(decoder.decode(buffers[1]));
        String[] firstpart = payload.split(",");
        String[] secondpart = firstpart[0].split(":");
        System.out.println(secondpart[1]);
        String email = secondpart[1].substring(1,secondpart[1].length()-1);

        User user = userRepository.findByEmail(email).get();


        AuthenticationOuterClass.AuthenticationResponse response =
                AuthenticationOuterClass.AuthenticationResponse.newBuilder().setUuid(String.valueOf(user.getId())).build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }

}
