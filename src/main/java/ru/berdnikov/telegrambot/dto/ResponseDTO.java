package ru.berdnikov.telegrambot.dto;


public class ResponseDTO {
    private String title;
    private String price;
    private String image_url;
    private String link;

    public ResponseDTO(String title, String price, String image_url, String link) {
        this.title = title;
        this.price = price;
        this.image_url = image_url;
        this.link = link;
    }

    public ResponseDTO() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return "ResponseDTO{" +
                "title='" + title + '\'' +
                ", price='" + price + '\'' +
                ", image_url='" + image_url + '\'' +
                ", link='" + link + '\'' +
                '}';
    }
}
