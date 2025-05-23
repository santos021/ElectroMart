package com.skb.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.skb.model.SliderImage;
import com.skb.repository.SliderImageRepository;
import com.skb.service.SliderImageService;

@Service
public class SliderImageServiceImpl implements SliderImageService {
	
	@Autowired
	private SliderImageRepository sImageRepository;

	@Override
	public void save(SliderImage image) {
		sImageRepository.save(image);
		
	}

	@Override
	public List<SliderImage> getAll() {
		return sImageRepository.findAll();
	}

	@Override
	public SliderImage getById(Long id) {
		return sImageRepository.findById(id).orElse(null);
	}

	@Override
	public void deleteById(Long id) {
		sImageRepository.deleteById(id);
		
	}
	
}
