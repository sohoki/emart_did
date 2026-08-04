import React, { useCallback } from 'react';
import { useIdCheck } from '@/hooks/use-id-check.js';
import URL from '@/constants/URL.jsx';
import UseSwitch from '@/components/Common/IosSwitch.jsx';

const CodeFormModal = ({
	open,
	form,
	setForm,
	onClose,
	onSubmit,
}) => {

	const { handleIdCheck } = useIdCheck(URL.CODE_ID_CHECK, '분류코드');

	const onIdCheck = useCallback(async () => {
		await handleIdCheck(form.codeId, setForm);
	}, [form.codeId, setForm, handleIdCheck]);

	const updateForm = useCallback((payload) => {
		setForm((prev) => ({ ...prev, ...payload }));
	}, [setForm]);

	const handleCodeIdChange = useCallback((e) => {
		const filtered = e.target.value.replace(/[^A-Za-z0-9_\-.]/g, '');
		updateForm({ codeId: filtered });
	}, [updateForm]);

	if (!open) return null;

	return (
		<>
			<div className="modal-backdrop-custom" onClick={onClose} />
			<div className="modal-custom">
				<div
					className="modal-dialog modal-dialog-centered"
					style={{ width: 830, maxWidth: '55%', backgroundColor: 'var(--bs-body-bg, #fff)' }}
				>
					<div className="modal-content">
						<div className="modal-header">
							<div className="modal-title">
								<h2 className="modal-title__title">
									분류 코드 {form.mode === 'Ins' ? '등록' : '수정'}
								</h2>
							</div>
							<button
								type="button"
								className="modal-close"
								aria-label="Close"
								onClick={onClose}
							/>
						</div>

						<div className="modal-body">
							<div className="modal-body__content">
								<div className="row input-box-wrap">

									{/* 코드 */}
									<div className="col-6">
										<div className="input-box">
											<label className="form-label">
												코드 <span className="text-danger">*</span>
											</label>
											<div className="input-group">
												<input
													id="codeId"
													name="codeId"
													placeholder="코드를 입력해주세요."
													type="text"
													className="form-control"
													value={form.codeId}
													readOnly={form.mode !== 'Ins'}
													disabled={form.mode !== 'Ins'}
													onChange={handleCodeIdChange}
												/>
												{form.mode === 'Ins' && (
													<button
														type="button"
														className="btn btn-primary btn-default__blue"
														onClick={onIdCheck}
													>
														중복체크
													</button>
												)}
											</div>
										</div>
									</div>

									{/* 코드명 */}
									<div className="col-6">
										<div className="input-box">
											<label className="form-label">
												코드명 <span className="text-danger">*</span>
											</label>
											<input
												id="codeIdNm"
												name="codeIdNm"
												type="text"
												className="form-control"
												value={form.codeIdNm}
												onChange={(e) => updateForm({ codeIdNm: e.target.value })}
											/>
										</div>
									</div>

									{/* 사용유무 */}
									<div className="col-6">
										<div className="input-box">
											<label className="form-label">사용</label>
											<div className="input-group">
                                                <UseSwitch
                                                    value={form.useAt}
                                                    name="useAt"
                                                    onChange={updateForm}
                                                    className="form-control"
                                                    onText="사용"
                                                    offText="사용안함"
                                                />
                                            </div>
										</div>
									</div>

									{/* 설명 */}
									<div className="col-12">
										<div className="input-box">
											<label className="form-label">설명</label>
											<input
												id="codeIdDc"
												name="codeIdDc"
												type="text"
												className="form-control"
												value={form.codeIdDc}
												onChange={(e) => updateForm({ codeIdDc: e.target.value })}
											/>
										</div>
									</div>

								</div>
							</div>
						</div>

						<div className="modal-footer">
							<div className="modal-footer__right">
								<button
									type="button"
									className="btn btn-cancel"
									onClick={onClose}
								>
									닫기
								</button>
								<button
									type="button"
									className="btn btn-primary btn-action__blue"
									onClick={onSubmit}
								>
									저장
								</button>
							</div>
						</div>

					</div>
				</div>
			</div>
		</>
	);
};

export default CodeFormModal;
