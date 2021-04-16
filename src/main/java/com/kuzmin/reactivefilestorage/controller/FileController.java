package com.kuzmin.reactivefilestorage.controller;

import org.springframework.data.mongodb.gridfs.ReactiveGridFsOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.http.ResponseEntity.ok;

import java.util.Map;

@RestController
@RequestMapping(value = "api/files")
public class FileController {
    private final ReactiveGridFsOperations gridFsOperations;

    public FileController(ReactiveGridFsOperations gridFsOperations) {
        this.gridFsOperations = gridFsOperations;
    }

    @PostMapping("")
    public Mono<ResponseEntity> upload(@RequestPart Mono<FilePart> fileParts) {
        return fileParts
                .flatMap(part -> this.gridFsOperations.store(part.content(), part.filename()))
                .map((id) -> ok().body(Map.of("id", id.toHexString())));
    }

    @GetMapping("{id}")
    public Flux<Void> read(@PathVariable String id, ServerWebExchange exchange) {
        return this.gridFsOperations.findOne(query(where("_id").is(id)))
                .log()
                .flatMap(gridFsOperations::getResource)
                .flatMapMany(r -> exchange.getResponse().writeWith(r.getDownloadStream()));
    }
}
