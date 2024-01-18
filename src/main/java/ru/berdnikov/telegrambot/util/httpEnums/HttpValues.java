package ru.berdnikov.telegrambot.util.httpEnums;

public enum HttpValues {
    FILTORG_URL(HttpURL.FILTORG_URL,
            "https://filtorg.ru/?subcats=Y&pcode_from_q=Y&pshort=Y&pfull=Y&pname=Y&pkeywords=Y&search_performed=Y&dispatch=products.search&q=",
            "div.ty-grid-list__item",
            "div.ty-grid-list__item-name a.product-title",
            "bdi",
            "span.ty-price-num",
            "span.ty-price-num",
            "img.ty-pict",
            "img.ty-pict",
            "src",
            "src",
            "",
            "href",
            ".ty-grid-list__item-name a",
            ""
    ),
    _76MONET_URL(HttpURL._76MONET_URL,
            "https://76monet.ru/search?q=",
            ".product_card",
            ".product_card-title a",
            ".product-title",
            ".product_card-price",
            ".product-prices span",
            "img.product_card-image",
            ".product-image a",
            "data-src",
            "href",
            "product_card-thumb--contain",
            "href",
            ".col-6 a",
            "https://76monet.ru/product/"
    );

    public final HttpURL httpURL;
    public final String url;
    public final String productElements;
    public final String title;
    public final String title_header;
    public final String price;
    public final String price_header;
    public final String imageElements;
    public final String image_header;
    public final String attr;
    public final String attr_header;
    public final String link;
    public final String ref;
    public final String linkElements;
    public final String baseSearchUrl;


    HttpValues(HttpURL httpURL,
               String url,
               String productElements,
               String title,
               String title_header,
               String price,
               String price_header,
               String imageElements,
               String image_header,
               String attr,
               String attr_header,
               String link,
               String ref,
               String linkElements,
               String baseSearchUrl) {
        this.httpURL = httpURL;
        this.url = url;
        this.productElements = productElements;
        this.title = title;
        this.title_header = title_header;
        this.price = price;
        this.price_header = price_header;
        this.imageElements = imageElements;
        this.image_header = image_header;
        this.attr = attr;
        this.attr_header = attr_header;
        this.link = link;
        this.ref = ref;
        this.linkElements = linkElements;
        this.baseSearchUrl = baseSearchUrl;
    }
}
