package com.skb.service;

import java.util.List;

import com.skb.model.SliderImage;

public interface SliderImageService {
	void save(SliderImage image);
    List<SliderImage> getAll();
    SliderImage getById(Long id);
    void deleteById(Long id);
}
