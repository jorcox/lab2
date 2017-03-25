package es.unizar.tmdad.lab2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.messaging.handler.annotation.MessageMapping;

import es.unizar.tmdad.lab2.service.TwitterLookupService;
import es.unizar.tmdad.lab2.Query;

@Controller
public class SearchController {

    @Autowired
    TwitterLookupService twitter;

    @RequestMapping("/")
    public String greeting() {
        return "index";
    }

    @ResponseBody
    @MessageMapping("/search")
    public void search(Query q) {
        twitter.search(q.getQuery());
    }
}