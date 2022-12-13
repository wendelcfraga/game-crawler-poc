package com.wendel.gamecrawler.controllers;

import com.wendel.gamecrawler.creds.Credenciais;
import com.wendel.gamecrawler.models.Name;
import com.wendel.gamecrawler.models.Post;
import com.wendel.gamecrawler.repositories.postRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/postagens")
public class PostController {
    private static HttpURLConnection conn;
    
    @Autowired
    private postRepository _postRepository;

    @GetMapping
    private List<Post> listarPosts() {
        return _postRepository.findAll();
    }

    @Value("${google.api.key}")
    private String googleKey;

    @PostMapping
    private Post criarPost(@RequestBody Name nome) {
        Post jogo = new Post();
        Credenciais chaves = new Credenciais();

        
        try {
            Document html = Jsoup.connect("https://1337x.to/category-search/" + nome.getNome() + "/Games/1/").userAgent("Mozilla").get();
            Element primeiroResultado = html.getElementsByTag("tr").next().first();

            String nomeJogo = primeiroResultado.getElementsByClass("coll-1 name").text();
            String linkJogo = "https://1337x.to" + primeiroResultado.getElementsByClass("coll-1 name").first()
                .getElementsByTag("a")
                .next().attr("href");
            String autorPost = primeiroResultado.getElementsByClass("coll-5 uploader").text();    
            
            jogo.setNome(nomeJogo);
            jogo.setAutor(autorPost);

            Document paginaJogo = Jsoup.connect(linkJogo).get();
            String downloadLink = paginaJogo.select("ul.dropdown-menu").first().children().first().children().first().attr("href");
            String tamanhoJogo = paginaJogo.getElementsByClass("list")
                .next()
                .prev()
                .first()
                .firstElementChild()
                .nextElementSibling()
                .nextElementSibling()
                .nextElementSibling()
                .children()
                .first()
                .nextElementSibling()
                .text();
            
                
            
            jogo.setMagnetLink(downloadLink);
            jogo.setTamanho(tamanhoJogo);

            BufferedReader leitor;
            String linha;
            StringBuilder responseContent = new StringBuilder();
            try {
                URL url = new URL("https://www.googleapis.com/customsearch/v1?key=" + googleKey + "&cx=" + chaves.googleId + "&q=" + URLEncoder.encode(nome.getNome(), StandardCharsets.UTF_8) + "&searchType=image");
                conn = (HttpURLConnection) url.openConnection();
                
                // configuraçao request
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                
                // verificar resposta servidor
                int status = conn.getResponseCode();
                
                if (status >= 300) {
                    leitor = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    while ((linha = leitor.readLine()) != null) {
                        responseContent.append(linha);
                    }
                    leitor.close();
                }
                else {
                    leitor = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((linha = leitor.readLine()) != null) {
                        responseContent.append(linha);
                    }
                    leitor.close();
                }
    
            
    
                JSONObject imageLinks = new JSONObject(responseContent.toString());
                JSONArray items = imageLinks.getJSONArray("items");
                
                JSONObject imgLoca = items.getJSONObject(0);
                String imgLink = imgLoca.getString("link");
                jogo.setImgLink(imgLink);
    
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                conn.disconnect();
            }

            Document pesquisaWiki = Jsoup.connect("https://pt.wikipedia.org/w/index.php?fulltext=Pesquisar&search=" + URLEncoder.encode(nome.getNome(), StandardCharsets.UTF_8) + "&title=Especial%3APesquisar&ns0=1").get();
            String linkWiki = "https://pt.wikipedia.org" + pesquisaWiki.getElementsByClass("mw-search-result-heading").first().getElementsByTag("a").attr("href");

            //pegar descriçao jogo
            //Document jogoWiki = Jsoup.connect(linkWiki).get();
            //String descricaoWiki = jogoWiki.getElementsByClass("mw-parser-output").first().child(3).text();

            jogo.setDescricao(linkWiki);

        } catch (Exception e) {
            System.out.println(e);
        }        

        return _postRepository.save(jogo);
        
    }
    
}
